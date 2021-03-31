package fi.thakki.sudokusolver.print

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.util.PuzzleTraverser

enum class BorderType {
    PUZZLE,
    REGION,
    CELL
}

data class Borders(
    val top: BorderType,
    val bottom: BorderType,
    val left: BorderType,
    val right: BorderType
)

class CellBorders(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)
    private val cellRegions = puzzle.cells.map { cell -> cell to puzzleTraverser.regionOf(cell) }.toMap()

    fun getCellBorders(): Map<Cell, Borders> =
        puzzle.cells.map { cell ->
            cell to Borders(
                top = cellBorderType(cell, puzzleTraverser::above),
                bottom = cellBorderType(cell, puzzleTraverser::below),
                left = cellBorderType(cell, puzzleTraverser::leftOf),
                right = cellBorderType(cell, puzzleTraverser::rightOf)
            )
        }.toMap()

    private fun cellBorderType(cell: Cell, traverserFunc: (Cell) -> Cell?): BorderType =
        traverserFunc(cell).let { nextCell ->
            when {
                nextCell == null -> BorderType.PUZZLE
                cellRegions[cell] != cellRegions[nextCell] -> BorderType.REGION
                else -> BorderType.CELL
            }
        }
}
