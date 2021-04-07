package fi.thakki.sudokusolver.print

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.util.SudokuTraverser

enum class BorderType {
    SUDOKU,
    REGION,
    CELL
}

data class Borders(
    val top: BorderType,
    val bottom: BorderType,
    val left: BorderType,
    val right: BorderType
)

class CellBorders(private val sudoku: Sudoku) {

    private val sudokuTraverser = SudokuTraverser(sudoku)
    private val cellRegions = sudoku.cells.map { cell -> cell to sudokuTraverser.regionOf(cell) }.toMap()

    fun getCellBorders(): Map<Cell, Borders> =
        sudoku.cells.map { cell ->
            cell to Borders(
                top = cellBorderType(cell, sudokuTraverser::above),
                bottom = cellBorderType(cell, sudokuTraverser::below),
                left = cellBorderType(cell, sudokuTraverser::leftOf),
                right = cellBorderType(cell, sudokuTraverser::rightOf)
            )
        }.toMap()

    private fun cellBorderType(cell: Cell, traverserFunc: (Cell) -> Cell?): BorderType =
        traverserFunc(cell).let { nextCell ->
            when {
                nextCell == null -> BorderType.SUDOKU
                cellRegions[cell] != cellRegions[nextCell] -> BorderType.REGION
                else -> BorderType.CELL
            }
        }
}
