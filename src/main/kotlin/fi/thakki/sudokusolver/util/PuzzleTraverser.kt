package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Band
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.Stack

class PuzzleTraverser(private val puzzle: Puzzle) {

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
}
