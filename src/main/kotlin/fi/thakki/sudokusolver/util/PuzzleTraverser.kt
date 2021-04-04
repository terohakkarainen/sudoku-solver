package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Band
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.Stack

@Suppress("TooManyFunctions")
class PuzzleTraverser(private val puzzle: Puzzle) {

    // TODO also a variant with arguments (x: Coordinate, y: Coordinate)?
    fun cellAt(coordinates: Coordinates): Cell =
        intersectionOf(
            puzzle.bands[coordinates.y],
            puzzle.stacks[coordinates.x]
        )

    private fun intersectionOf(band: Band, stack: Stack): Cell =
        checkNotNull(
            band.find {
                it.coordinates.x == stack.first().coordinates.x
            }
        )

    fun intersectionsOf(first: Cell, second: Cell): Pair<Cell, Cell> =
        Pair(
            cellAt(Coordinates(first.coordinates.x, second.coordinates.y)),
            cellAt(Coordinates(second.coordinates.x, first.coordinates.y))
        )

    fun bandOf(cell: Cell): Band =
        puzzle.bands[cell.coordinates.y]

    fun stackOf(cell: Cell): Stack =
        puzzle.stacks[cell.coordinates.x]

    fun regionOf(cell: Cell): Region =
        puzzle.regions.single { region -> cell in region }

    fun above(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x, cell.coordinates.y + 1)

    fun below(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x, cell.coordinates.y - 1)

    fun leftOf(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x - 1, cell.coordinates.y)

    fun rightOf(cell: Cell): Cell? =
        cellInCoordinates(cell.coordinates.x + 1, cell.coordinates.y)

    private fun cellInCoordinates(x: Coordinate, y: Coordinate): Cell? =
        if (isInPuzzleRange(x) && isInPuzzleRange(y)) {
            cellAt(Coordinates(x, y))
        } else null

    private fun isInPuzzleRange(value: Int): Boolean =
        value in (0 until puzzle.dimension.value)

    fun inSameCellCollection(first: Cell, second: Cell): Boolean =
        inSameBand(first, second) || inSameStack(first, second) || inSameRegion(first, second)

    fun inSameBand(vararg cells: Cell): Boolean =
        cells.map { it.coordinates.y }.distinct().size == 1

    fun inSameStack(vararg cells: Cell): Boolean =
        cells.map { it.coordinates.x }.distinct().size == 1

    fun inSameRegion(vararg cells: Cell): Boolean =
        cells.map { regionOf(it) }.distinct().size == 1
}
