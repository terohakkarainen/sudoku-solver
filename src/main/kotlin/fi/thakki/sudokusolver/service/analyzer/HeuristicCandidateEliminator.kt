package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.service.PuzzleRevisionService

class HeuristicCandidateEliminator(private val puzzle: Puzzle) {

    fun eliminateCandidates(): AnalyzeResult =
        when {
            eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            else -> AnalyzeResult.NoChanges
        }

    private fun eliminateBiChoiceCellCandidatesByTestingHowRemovalAffectsAnalyze(): AnalyzeResult =
        puzzle.cells.cellsWithoutValue()
            .filter { cell -> cell.analysis.candidates.size == 2 }
            .let { biChoiceCells ->
                if (biChoiceCells.isNotEmpty()) {
                    AnalyzeResult.combinedResultOf(
                        biChoiceCells.map(this::testBiChoiceCellCandidates)
                    )
                } else AnalyzeResult.NoChanges
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
                        PuzzleMutationService(puzzle).toggleCandidate(
                            cell.coordinates,
                            cell.analysis.candidates.last()
                        )
                        AnalyzeResult.CandidatesEliminated
                    }
                    !listOfConstraintViolationOccurred.first() && listOfConstraintViolationOccurred.last() -> {
                        // Second candidate could not be removed, so remove first candidate.
                        PuzzleMutationService(puzzle).toggleCandidate(
                            cell.coordinates,
                            cell.analysis.candidates.first()
                        )
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
            PuzzleMutationService(puzzleSnapshot).toggleCandidate(coordinates, candidate)
            // Heuristic analyze must be excluded so that we don't end up in multi-level candidate testing.
            PuzzleAnalyzer(puzzleSnapshot).analyze(rounds = Int.MAX_VALUE, doHeuristicAnalysis = false)
            // Analyze passed without conflicts.
            false
        } catch (e: Exception) {
            true
        }
}
