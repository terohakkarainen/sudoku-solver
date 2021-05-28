package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService
import fi.thakki.sudokusolver.engine.service.mutation.SudokuSerializationService

class HeuristicCandidateEliminator(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(this::eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze)

    private fun eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze(): AnalyzeResult =
        sudoku.cells.cellsWithoutValue()
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
                    SudokuSerializationService.copyOf(sudoku),
                    cell.coordinates,
                    candidate
                )
            }.let { listOfConstraintViolationOccurred ->
                when {
                    listOfConstraintViolationOccurred.first() && !listOfConstraintViolationOccurred.last() -> {
                        // First candidate could not be removed, so remove second candidate.
                        SudokuMutationService(sudoku).removeCandidate(
                            cell.coordinates,
                            cell.analysis.candidates.last()
                        ) { message ->
                            messageBroker.message("Second candidate eliminated by heuristics: $message")
                        }
                        AnalyzeResult.CandidatesEliminated
                    }
                    !listOfConstraintViolationOccurred.first() && listOfConstraintViolationOccurred.last() -> {
                        // Second candidate could not be removed, so remove first candidate.
                        SudokuMutationService(sudoku).removeCandidate(
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
        sudokuSnapshot: Sudoku,
        coordinates: Coordinates,
        candidate: Symbol
    ): Boolean =
        try {
            SudokuMutationService(sudokuSnapshot).removeCandidate(coordinates, candidate)
            // Heuristic analyze must be excluded so that we don't end up in multi-level candidate testing.
            SudokuAnalyzer(
                sudoku = sudokuSnapshot,
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
}
