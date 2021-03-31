package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class SimpleCandidateUpdater(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

    fun updateCandidates(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.cells.cellsWithoutValue()
                .map { cell -> updateCandidatesForCell(cell) }
        )

    private fun updateCandidatesForCell(cell: Cell): AnalyzeResult =
        cell.analysis.candidates.let { existingCandidates ->
            existingCandidates.subtract(symbolsTakenFromCellPerspective(cell)).let { newCandidates ->
                if (newCandidates != existingCandidates) {
                    PuzzleMutationService(puzzle).setCellCandidates(cell.coordinates, newCandidates) { message ->
                        messageBroker.message("SimpleCandidateUpdater: $message")
                    }
                    AnalyzeResult.CandidatesEliminated
                } else AnalyzeResult.NoChanges
            }
        }

    private fun symbolsTakenFromCellPerspective(cell: Cell): Set<Symbol> =
        PuzzleTraverser(puzzle).let { puzzleTraverser ->
            listOf(
                puzzleTraverser::bandOf,
                puzzleTraverser::stackOf,
                puzzleTraverser::regionOf
            ).map { traverseFunc -> traverseFunc(cell).cellsWithValue().map { checkNotNull(it.value) }.toSet() }
                .reduce { acc, s -> acc.union(s) }
        }
}
