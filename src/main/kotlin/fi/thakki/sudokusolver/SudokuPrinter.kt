package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.canvas.Canvas
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.Painter
import fi.thakki.sudokusolver.canvas.Pixel
import fi.thakki.sudokusolver.canvas.Size
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SudokuPrinter(private val puzzle: Puzzle) {

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

    private val puzzleTraverser = PuzzleTraverser(puzzle)
    private val cellRegions = puzzle.cells.map { cell -> cell to puzzleTraverser.regionOf(cell) }.toMap()
    private val borders = getCellBorders(puzzle)
    private val candidatesPerRow = sqrt(puzzle.dimension.value.toDouble()).roundToInt()
    private val cellWidth = 2 * candidatesPerRow + 1
    private val cellHeight = ceil(puzzle.symbols.size.toDouble() / candidatesPerRow.toDouble()).toInt()

    private val canvas =
        Size(
            width = VERTICAL_RULER_OFFSET_LENGTH * 2 +
                    puzzle.dimension.value * cellWidth +
                    puzzle.dimension.value,
            height = puzzle.dimension.value * cellHeight + puzzle.dimension.value + 3
        ).let { puzzleSize ->
            Canvas(
                size = puzzleSize,
                numberOfLayers = 4
            )
        }

    private fun getCellBorders(puzzle: Puzzle): Map<Cell, Borders> =
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

    fun printPuzzle(highlightedSymbol: Symbol? = null) {
        val highlightLayer = canvas.layers[0]
        val valueLayer = canvas.layers[1]
        val regionLayer = canvas.layers[2]
        val gridLayer = canvas.layers[3]

        canvas.painterForLayer(gridLayer)
            .let { gridPainter ->
                paintHorizontalRulers(gridPainter, listOf(0, canvas.size.height - 1), Color.DEFAULT)
                paintVerticalRulers(gridPainter, listOf(1, canvas.size.width - 1), Color.DEFAULT)
                paintGrid(gridPainter, Color.DEFAULT)
            }

        canvas.painterForLayer(regionLayer)
            .let { regionPainter ->
                borders.entries.forEach { entry ->
                    paintCellBorders(regionPainter, entry.key, entry.value, Color.BLUE)
                }
                var i = 0
                puzzle.regions.forEach { region ->
                    region.cells.forEach { cell ->
                        regionPainter.rectangle(
                            bottomLeft = cellBottomLeftScreenCoordinates(cell.coordinates),
                            topRight = cellTopRightScreenCoordinates(cell.coordinates),
                            bgColor = if (i % 2 == 0) Color.DARK_GRAY else Color.BLACK
                        )
                    }
                    i++
                }
            }

        canvas.painterForLayer(valueLayer)
            .let { valuePainter ->
                puzzle.cells.forEach { cell ->
                    if (cell.hasValue()) {
                        valuePainter.pixel(
                            cell.value.toString(),
                            cellMiddleScreenCoordinates(cell.coordinates),
                            if (cell.type == CellValueType.GIVEN) Color.BLUE else Color.GREEN
                        )
                    } else {
                        puzzle.symbols.sorted().forEach { symbol ->
                            if (cell.analysis.candidates.contains(symbol)) {
                                valuePainter.pixel(
                                    symbol.toString(),
                                    candidateScreenCoordinates(cell, symbol),
                                    Color.CYAN
                                )
                            }
                        }
                    }
                }
            }

        highlightedSymbol?.let { symbol ->
            canvas.painterForLayer(highlightLayer)
                .let { highlightPainter ->
                    // Highlighted symbols.
                    puzzle.cells.cellsWithoutValue().forEach { cell ->
                        highlightPainter.textArea(
                            cellBottomLeftScreenCoordinates(cell.coordinates),
                            cellTopRightScreenCoordinates(cell.coordinates),
                            " "
                        )
                        if (cell.analysis.candidates.contains(symbol)) {
                            highlightPainter.pixel(
                                symbol.toString(),
                                candidateScreenCoordinates(cell, symbol),
                                Color.RED
                            )
                        }
                    }

                    // Strong links.
                    puzzle.allCellCollections()
                        .flatMap { it.analysis.strongLinks }
                        .filter { it.symbol == symbol }
                        .forEach { strongLink ->
                            highlightPainter.line(
                                from = candidateScreenCoordinates(strongLink.firstCell, strongLink.symbol),
                                to = candidateScreenCoordinates(strongLink.secondCell, strongLink.symbol),
                                bgColor = Color.LIGHT_YELLOW
                            )
                        }

                    // Strong link chains.
                    puzzle.analysis.strongLinkChains
                        .filter { it.symbol == symbol }
                        .forEach { linkChain ->
                            linkChain.strongLinks.forEach { strongLink ->
                                highlightPainter.line(
                                    from = candidateScreenCoordinates(strongLink.firstCell, linkChain.symbol),
                                    to = candidateScreenCoordinates(strongLink.secondCell, linkChain.symbol),
                                    bgColor = Color.LIGHT_MAGENTA
                                )
                            }
                        }
                }
        }

        canvas.copyToScreen()
    }

    private fun paintHorizontalRulers(painter: Painter, rows: List<Coordinate>, color: Color) {
        rows.forEach { y ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = cellMiddleScreenCoordinates(Coordinates(index, 0))
                painter.pixel(
                    index.toString(),
                    Coordinates(cellMiddle.x, y),
                    color
                )
            }
        }
    }

    private fun paintVerticalRulers(painter: Painter, columns: List<Coordinate>, color: Color) {
        columns.forEach { x ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = cellMiddleScreenCoordinates(Coordinates(0, index))
                painter.pixel(
                    index.toString(),
                    Coordinates(x, cellMiddle.y),
                    color
                )
            }
        }
    }

    private fun paintGrid(painter: Painter, color: Color) {
        // Horizontal lines
        (0 until puzzle.dimension.value).forEach { y ->
            painter.perpendicularLine(
                from = cellBorderTopLeftScreenCoordinates(Coordinates(0, y)),
                to = cellBorderTopRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, y)),
                character = Pixel.HORIZ_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.perpendicularLine(
            from = cellBorderBottomLeftScreenCoordinates(Coordinates(0, 0)),
            to = cellBorderBottomRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, 0)),
            character = Pixel.HORIZ_LIGHT_LINE,
            fgColor = color
        )

        // Vertical lines
        (0 until puzzle.dimension.value).forEach { x ->
            painter.perpendicularLine(
                from = cellBorderBottomLeftScreenCoordinates(Coordinates(x, 0)),
                to = cellBorderTopLeftScreenCoordinates(Coordinates(x, puzzle.dimension.value - 1)),
                character = Pixel.VERT_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.perpendicularLine(
            from = cellBorderBottomRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, 0)),
            to = cellBorderTopRightScreenCoordinates(
                Coordinates(puzzle.dimension.value - 1, puzzle.dimension.value - 1)
            ),
            character = Pixel.VERT_LIGHT_LINE,
            fgColor = color
        )
    }

    private fun paintCellBorders(painter: Painter, cell: Cell, borders: Borders, color: Color) {
        when (borders.top) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = cellBorderTopLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderTopRightScreenCoordinates(cell.coordinates),
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.left) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = cellBorderBottomLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderTopLeftScreenCoordinates(cell.coordinates),
                    character = Pixel.VERT_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.bottom) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = cellBorderBottomLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderBottomRightScreenCoordinates(cell.coordinates),
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.right) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = cellBorderBottomRightScreenCoordinates(cell.coordinates),
                    to = cellBorderTopRightScreenCoordinates(cell.coordinates),
                    character = Pixel.VERT_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
    }

    private fun cellMiddleScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) +
                    (cellWidth.toFloat() / 2f).roundToInt() + VERTICAL_RULER_OFFSET_LENGTH,
            y = 1 + coordinates.y * (cellHeight + 1) + ((cellHeight + 1).toFloat() / 2f).roundToInt()
        )

    private fun cellBorderTopLeftScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + VERTICAL_RULER_OFFSET_LENGTH,
            y = coordinates.y * (cellHeight + 1) + cellHeight + 2
        )

    private fun cellBorderBottomLeftScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + VERTICAL_RULER_OFFSET_LENGTH,
            y = coordinates.y * (cellHeight + 1) + 1
        )

    private fun cellBottomLeftScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + VERTICAL_RULER_OFFSET_LENGTH + 1,
            y = coordinates.y * (cellHeight + 1) + 2
        )

    private fun cellBorderTopRightScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH + 1,
            y = coordinates.y * (cellHeight + 1) + cellHeight + 2
        )

    private fun cellTopRightScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH,
            y = coordinates.y * (cellHeight + 1) + cellHeight + 1
        )

    private fun cellBorderBottomRightScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH + 1,
            y = coordinates.y * (cellHeight + 1) + 1
        )

    private fun candidateScreenCoordinates(cell: Cell, symbol: Symbol): Coordinates {
        val allSymbolsChunked = puzzle.symbols.chunked(candidatesPerRow)
        val rowIndex = allSymbolsChunked.indexOfFirst { it.contains(symbol) }
        val columnIndex = allSymbolsChunked[rowIndex].indexOf(symbol)
        val bottomLeft = cellBottomLeftScreenCoordinates(cell.coordinates)
        return Coordinates(
            x = bottomLeft.x + columnIndex * 2 + 1,
            y = bottomLeft.y + (allSymbolsChunked.size - 1 - rowIndex)
        )
    }

    companion object {
        private const val VERTICAL_RULER_OFFSET_LENGTH = 3
    }
}
