package fi.thakki.sudokusolver.print

import fi.thakki.sudokusolver.canvas.Canvas
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.Painter
import fi.thakki.sudokusolver.canvas.Pixel
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

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
    private val rectanglesAndCoordinates = RectanglesAndCoordinates(puzzle)
    private val cellRegions = puzzle.cells.map { cell -> cell to puzzleTraverser.regionOf(cell) }.toMap()
    private val borders = getCellBorders(puzzle)
    private val canvas = Canvas(rectanglesAndCoordinates.canvasSize(), 4)

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
                            rectangle = rectanglesAndCoordinates.cellRectangle(cell),
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
                            rectanglesAndCoordinates.cellMiddleScreenCoordinates(cell.coordinates),
                            if (cell.type == CellValueType.GIVEN) Color.BLUE else Color.GREEN
                        )
                    } else {
                        puzzle.symbols.sorted().forEach { symbol ->
                            if (cell.analysis.candidates.contains(symbol)) {
                                valuePainter.pixel(
                                    symbol.toString(),
                                    rectanglesAndCoordinates.candidateScreenCoordinates(cell, symbol),
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
                            rectanglesAndCoordinates.cellRectangle(cell),
                            " "
                        )
                        if (cell.analysis.candidates.contains(symbol)) {
                            highlightPainter.pixel(
                                symbol.toString(),
                                rectanglesAndCoordinates.candidateScreenCoordinates(cell, symbol),
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
                                from = rectanglesAndCoordinates.candidateScreenCoordinates(
                                    strongLink.firstCell,
                                    strongLink.symbol
                                ),
                                to = rectanglesAndCoordinates.candidateScreenCoordinates(
                                    strongLink.secondCell,
                                    strongLink.symbol
                                ),
                                bgColor = Color.LIGHT_YELLOW
                            )
                        }

                    // Strong link chains.
                    puzzle.analysis.strongLinkChains
                        .filter { it.symbol == symbol }
                        .forEach { linkChain ->
                            linkChain.strongLinks.forEach { strongLink ->
                                highlightPainter.line(
                                    from = rectanglesAndCoordinates.candidateScreenCoordinates(
                                        strongLink.firstCell,
                                        linkChain.symbol
                                    ),
                                    to = rectanglesAndCoordinates.candidateScreenCoordinates(
                                        strongLink.secondCell,
                                        linkChain.symbol
                                    ),
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
                val cellMiddle = rectanglesAndCoordinates.cellMiddleScreenCoordinates(Coordinates(index, 0))
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
                val cellMiddle = rectanglesAndCoordinates.cellMiddleScreenCoordinates(Coordinates(0, index))
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
                from = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(0, y)).topLeft,
                to = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(puzzle.dimension.value - 1, y)).topRight,
                character = Pixel.HORIZ_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.perpendicularLine(
            from = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(0, 0)).bottomLeft,
            to = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(puzzle.dimension.value - 1, 0)).bottomRight,
            character = Pixel.HORIZ_LIGHT_LINE,
            fgColor = color
        )

        // Vertical lines
        (0 until puzzle.dimension.value).forEach { x ->
            painter.perpendicularLine(
                from = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(x, 0)).bottomLeft,
                to = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(x, puzzle.dimension.value - 1)).topLeft,
                character = Pixel.VERT_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.perpendicularLine(
            from = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(puzzle.dimension.value - 1, 0)).bottomRight,
            to = rectanglesAndCoordinates.cellBorderRectangle(
                Coordinates(
                    puzzle.dimension.value - 1,
                    puzzle.dimension.value - 1
                )
            ).topRight,
            character = Pixel.VERT_LIGHT_LINE,
            fgColor = color
        )
    }

    private fun paintCellBorders(painter: Painter, cell: Cell, borders: Borders, color: Color) {
        when (borders.top) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = rectanglesAndCoordinates.cellBorderRectangle(cell).topLeft,
                    to = rectanglesAndCoordinates.cellBorderRectangle(cell).topRight,
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.left) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = rectanglesAndCoordinates.cellBorderRectangle(cell).bottomLeft,
                    to = rectanglesAndCoordinates.cellBorderRectangle(cell).topLeft,
                    character = Pixel.VERT_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.bottom) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = rectanglesAndCoordinates.cellBorderRectangle(cell).bottomLeft,
                    to = rectanglesAndCoordinates.cellBorderRectangle(cell).bottomRight,
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.right) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = rectanglesAndCoordinates.cellBorderRectangle(cell).bottomRight,
                    to = rectanglesAndCoordinates.cellBorderRectangle(cell).topRight,
                    character = Pixel.VERT_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
    }
}
