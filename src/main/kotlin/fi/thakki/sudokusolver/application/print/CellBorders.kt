package fi.thakki.sudokusolver.application.print

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.service.SudokuTraverser

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
    private val cellRegions: Map<Cell, Region> = sudoku.cells.associateWith { cell -> sudokuTraverser.regionOf(cell) }

    fun getCellBorders(): Map<Cell, Borders> =
        sudoku.cells.associateWith { cell ->
            Borders(
                top = cellBorderType(cell, sudokuTraverser::above),
                bottom = cellBorderType(cell, sudokuTraverser::below),
                left = cellBorderType(cell, sudokuTraverser::leftOf),
                right = cellBorderType(cell, sudokuTraverser::rightOf)
            )
        }

    private fun cellBorderType(cell: Cell, traverserFunc: (Cell) -> Cell?): BorderType =
        traverserFunc(cell).let { nextCell ->
            when {
                nextCell == null -> BorderType.SUDOKU
                cellRegions[cell] != cellRegions[nextCell] -> BorderType.REGION
                else -> BorderType.CELL
            }
        }
}
