package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Band
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.Stack

@Suppress("TooManyFunctions")
class SudokuTraverser(private val sudoku: Sudoku) {

    fun cellAt(x: Coordinate, y: Coordinate): Cell =
        cellAt(Coordinates(x, y))

    fun cellAt(coordinates: Coordinates): Cell =
        intersectionOf(
            sudoku.bands[coordinates.y],
            sudoku.stacks[coordinates.x]
        )

    private fun intersectionOf(band: Band, stack: Stack): Cell =
        checkNotNull(
            band.find {
                it.coordinates.x == stack.first().coordinates.x
            }
        )

    fun intersectionsOf(first: Cell, second: Cell): Pair<Cell, Cell> =
        Pair(
            cellAt(first.coordinates.x, second.coordinates.y),
            cellAt(second.coordinates.x, first.coordinates.y)
        )

    fun bandOf(cell: Cell): Band =
        sudoku.bands[cell.coordinates.y]

    fun stackOf(cell: Cell): Stack =
        sudoku.stacks[cell.coordinates.x]

    fun regionOf(cell: Cell): Region =
        sudoku.regions.single { region -> cell in region }

    fun above(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x, cell.coordinates.y + 1)

    fun below(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x, cell.coordinates.y - 1)

    fun leftOf(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x - 1, cell.coordinates.y)

    fun rightOf(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x + 1, cell.coordinates.y)

    private fun cellInCoordinates(x: Coordinate, y: Coordinate): Cell? =
        if (isWithinSudokuDimension(x) && isWithinSudokuDimension(y)) {
            cellAt(x, y)
        } else null

    private fun isWithinSudokuDimension(value: Int): Boolean =
        value in (0 until sudoku.dimension.value)

    fun inSameBand(vararg cells: Cell): Boolean =
        cells.map { it.coordinates.y }.distinct().size == 1

    fun inSameStack(vararg cells: Cell): Boolean =
        cells.map { it.coordinates.x }.distinct().size == 1

    fun inSameRegion(vararg cells: Cell): Boolean =
        cells.map { regionOf(it) }.distinct().size == 1
}
