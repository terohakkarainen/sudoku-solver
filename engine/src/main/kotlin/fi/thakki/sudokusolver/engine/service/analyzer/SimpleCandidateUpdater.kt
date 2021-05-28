package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService

class SimpleCandidateUpdater(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    fun updateCandidates(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            sudoku.cells.cellsWithoutValue()
                .map { cell -> updateCandidatesForCell(cell) }
        )

    private fun updateCandidatesForCell(cell: Cell): AnalyzeResult =
        cell.analysis.candidates.let { existingCandidates ->
            existingCandidates.subtract(symbolsTakenFromCellPerspective(cell)).let { newCandidates ->
                if (newCandidates != existingCandidates) {
                    SudokuMutationService(sudoku).setCellCandidates(cell.coordinates, newCandidates) { message ->
                        messageBroker.message("SimpleCandidateUpdater: $message")
                    }
                    AnalyzeResult.CandidatesEliminated
                } else AnalyzeResult.NoChanges
            }
        }

    private fun symbolsTakenFromCellPerspective(cell: Cell): Set<Symbol> =
        SudokuTraverser(sudoku).let { sudokuTraverser ->
            listOf(
                sudokuTraverser::bandOf,
                sudokuTraverser::stackOf,
                sudokuTraverser::regionOf
            ).map { traverseFunc -> traverseFunc(cell).cellsWithValue().map { checkNotNull(it.value) }.toSet() }
                .reduce { acc, s -> acc.union(s) }
        }
}
