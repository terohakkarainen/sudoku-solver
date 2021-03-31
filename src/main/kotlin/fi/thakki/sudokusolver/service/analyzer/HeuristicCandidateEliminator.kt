package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.service.PuzzleRevisionService

class HeuristicCandidateEliminator(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(this::eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze)

    private fun eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze(): AnalyzeResult =
        puzzle.cells.cellsWithoutValue()
            .filter { cell -> cell.analysis.candidates.size == 2 }
            .let { biChoiceCells ->
                biChoiceCells.forEach { biChoiceCell ->
                    val testResult = testBiChoiceCellCandidates(biChoiceCell)
                    if (testResult == AnalyzeResult.CandidatesEliminated) return testResult
                }
                AnalyzeResult.NoChanges
            }

    private fun testBiChoiceCellCandidates(cell: Cell): AnalyzeResult =
        cell.analysis.candidates
            .map { candidate ->
                candidateEliminationCausesConstraintViolation(
                    PuzzleRevisionService.copyOf(puzzle),
                    cell.coordinates,
                    candidate
                )
            }.let { listOfConstraintViolationOccurred ->
                when {
                    listOfConstraintViolationOccurred.first() && !listOfConstraintViolationOccurred.last() -> {
                        // First candidate could not be removed, so remove second candidate.
                        PuzzleMutationService(puzzle).removeCandidate(
                            cell.coordinates,
                            cell.analysis.candidates.last()
                        ) { message ->
                            messageBroker.message("Second candidate eliminated by heuristics: $message")
                        }
                        AnalyzeResult.CandidatesEliminated
                    }
                    !listOfConstraintViolationOccurred.first() && listOfConstraintViolationOccurred.last() -> {
                        // Second candidate could not be removed, so remove first candidate.
                        PuzzleMutationService(puzzle).removeCandidate(
                            cell.coordinates,
                            cell.analysis.candidates.first()
                        ) { message ->
                            messageBroker.message("First candidate eliminated by heuristics: $message")
                        }
                        AnalyzeResult.CandidatesEliminated
                    }
                    else -> AnalyzeResult.NoChanges
                }
            }

    @Suppress("TooGenericExceptionCaught")
    private fun candidateEliminationCausesConstraintViolation(
        puzzleSnapshot: Puzzle,
        coordinates: Coordinates,
        candidate: Symbol
    ): Boolean =
        try {
            PuzzleMutationService(puzzleSnapshot).removeCandidate(coordinates, candidate)
            // Heuristic analyze must be excluded so that we don't end up in multi-level candidate testing.
            PuzzleAnalyzer(
                puzzle = puzzleSnapshot,
                messageBroker = DiscardingMessageBroker
            ).analyze(
                rounds = Int.MAX_VALUE,
                doHeuristicAnalysis = false
            )
            // Analyze passed without conflicts.
            false
        } catch (e: Exception) {
            true
        }

    companion object {
        object DiscardingMessageBroker : PuzzleMessageBroker {
            override fun message(message: String) {
                // Nop.
            }

            override fun error(message: String) {
                // Nop.
            }
        }
    }
}
