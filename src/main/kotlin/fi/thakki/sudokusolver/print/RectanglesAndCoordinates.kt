package fi.thakki.sudokusolver.print

import fi.thakki.sudokusolver.canvas.Rectangle
import fi.thakki.sudokusolver.canvas.Size
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

class RectanglesAndCoordinates(private val puzzle: Puzzle) {

    private val candidatesPerRow = sqrt(puzzle.dimension.value.toDouble()).roundToInt()
    private val cellWidth = 2 * candidatesPerRow + 1
    private val cellHeight = ceil(puzzle.symbols.size.toDouble() / candidatesPerRow.toDouble()).toInt()

    val canvasSize = Size(
        width = VERTICAL_RULER_OFFSET_LENGTH * 2 + puzzle.dimension.value * cellWidth + puzzle.dimension.value,
        height = puzzle.dimension.value * cellHeight + puzzle.dimension.value + 3
    )

    fun cellBorderRectangle(cell: Cell): Rectangle =
        cellBorderRectangle(cell.coordinates)

    fun cellBorderRectangle(coordinates: Coordinates) =
        Rectangle(
            topLeft = Coordinates(
                x = coordinates.x * (cellWidth + 1) + VERTICAL_RULER_OFFSET_LENGTH,
                y = coordinates.y * (cellHeight + 1) + cellHeight + 2
            ),
            bottomRight = Coordinates(
                x = coordinates.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH + 1,
                y = coordinates.y * (cellHeight + 1) + 1
            )
        )

    fun cellRectangle(cell: Cell): Rectangle =
        cell.coordinates.let { coords ->
            Rectangle(
                topLeft = Coordinates(
                    x = coords.x * (cellWidth + 1) + VERTICAL_RULER_OFFSET_LENGTH + 1,
                    y = coords.y * (cellHeight + 1) + cellHeight + 1
                ),
                bottomRight = Coordinates(
                    x = coords.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH,
                    y = coords.y * (cellHeight + 1) + 2
                )
            )
        }

    fun cellMiddleScreenCoordinates(coordinates: Coordinates) =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) +
                    (cellWidth.toFloat() / 2f).roundToInt() + VERTICAL_RULER_OFFSET_LENGTH,
            y = 1 + coordinates.y * (cellHeight + 1) + ((cellHeight + 1).toFloat() / 2f).roundToInt()
        )

    fun candidateScreenCoordinates(cell: Cell, symbol: Symbol): Coordinates {
        val allSymbolsChunked = puzzle.symbols.chunked(candidatesPerRow)
        val rowIndex = allSymbolsChunked.indexOfFirst { it.contains(symbol) }
        val columnIndex = allSymbolsChunked[rowIndex].indexOf(symbol)
        val bottomLeft = cellRectangle(cell).bottomLeft
        return Coordinates(
            x = bottomLeft.x + columnIndex * 2 + 1,
            y = bottomLeft.y + (allSymbolsChunked.size - 1 - rowIndex)
        )
    }

    companion object {
        const val VERTICAL_RULER_OFFSET_LENGTH = 3
    }
}
