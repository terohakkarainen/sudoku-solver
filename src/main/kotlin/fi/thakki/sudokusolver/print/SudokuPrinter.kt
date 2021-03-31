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
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkChain
import fi.thakki.sudokusolver.model.Symbol

class SudokuPrinter(private val puzzle: Puzzle) {

    private val rectanglesAndCoordinates = RectanglesAndCoordinates(puzzle)
    private val borders = CellBorders(puzzle).getCellBorders()

    @Suppress("MagicNumber")
    fun printPuzzle(highlightedSymbol: Symbol? = null) {
        val canvas = Canvas(rectanglesAndCoordinates.canvasSize(), 4)

        val highlightLayer = canvas.layers[0]
        val valueLayer = canvas.layers[1]
        val regionLayer = canvas.layers[2]
        val gridLayer = canvas.layers[3]

        canvas.painterForLayer(gridLayer).let { gridPainter ->
            paintHorizontalRulers(gridPainter, listOf(0, canvas.size.height - 1), Color.DEFAULT)
            paintVerticalRulers(gridPainter, listOf(1, canvas.size.width - 1), Color.DEFAULT)
            paintGrid(gridPainter, Color.DEFAULT)
        }

        canvas.painterForLayer(regionLayer).let { regionPainter ->
            borders.entries.forEach { entry ->
                paintCellBorders(regionPainter, entry.key, entry.value, Color.BLUE)
            }
            var i = 0
            puzzle.regions.forEach { region ->
                paintRegion(regionPainter, region, if (i % 2 == 0) Color.DARK_GRAY else Color.BLACK)
                i++
            }
        }

        canvas.painterForLayer(valueLayer).let { valuePainter ->
            puzzle.cells.forEach { cell ->
                if (cell.hasValue()) {
                    paintCellWithValue(valuePainter, cell, Color.BLUE, Color.GREEN)
                } else {
                    paintCellWithoutValue(valuePainter, cell, Color.CYAN)
                }
            }
        }

        highlightedSymbol?.let { symbol ->
            canvas.painterForLayer(highlightLayer).let { highlightPainter ->
                puzzle.cells.cellsWithoutValue().forEach { cell ->
                    paintCellWithSingleCandidate(highlightPainter, cell, symbol, Color.CYAN)
                }
                puzzle.allCellCollections()
                    .flatMap { it.analysis.strongLinks }
                    .filter { it.symbol == symbol }
                    .forEach { strongLink ->
                        paintStrongLink(highlightPainter, strongLink, Color.LIGHT_YELLOW)
                    }
                puzzle.analysis.strongLinkChains
                    .filter { it.symbol == symbol }
                    .forEach { linkChain ->
                        paintStrongLinkChain(highlightPainter, linkChain, Color.LIGHT_MAGENTA)
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

    private fun paintRegion(painter: Painter, region: Region, bgColor: Color) {
        region.cells.forEach { cell ->
            painter.rectangle(
                rectangle = rectanglesAndCoordinates.cellRectangle(cell),
                bgColor = bgColor
            )
        }
    }

    private fun paintCellWithValue(
        painter: Painter,
        cell: Cell,
        givenValueColor: Color,
        setValueColor: Color
    ) {
        painter.pixel(
            cell.value.toString(),
            rectanglesAndCoordinates.cellMiddleScreenCoordinates(cell.coordinates),
            if (cell.type == CellValueType.GIVEN) givenValueColor else setValueColor
        )
    }

    private fun paintCellWithoutValue(painter: Painter, cell: Cell, candidateColor: Color) {
        puzzle.symbols.sorted().forEach { symbol ->
            if (cell.analysis.candidates.contains(symbol)) {
                painter.pixel(
                    symbol.toString(),
                    rectanglesAndCoordinates.candidateScreenCoordinates(cell, symbol),
                    candidateColor
                )
            }
        }
    }

    private fun paintCellWithSingleCandidate(
        painter: Painter,
        cell: Cell,
        candidate: Symbol,
        candidateColor: Color
    ) {
        painter.textArea(
            rectanglesAndCoordinates.cellRectangle(cell),
            Pixel.NO_VALUE
        )
        if (cell.analysis.candidates.contains(candidate)) {
            painter.pixel(
                candidate.toString(),
                rectanglesAndCoordinates.candidateScreenCoordinates(cell, candidate),
                candidateColor
            )
        }
    }

    private fun paintStrongLink(painter: Painter, strongLink: StrongLink, linkColor: Color) {
        painter.line(
            from = rectanglesAndCoordinates.candidateScreenCoordinates(
                strongLink.firstCell,
                strongLink.symbol
            ),
            to = rectanglesAndCoordinates.candidateScreenCoordinates(
                strongLink.secondCell,
                strongLink.symbol
            ),
            bgColor = linkColor
        )
    }

    private fun paintStrongLinkChain(painter: Painter, chain: StrongLinkChain, linkColor: Color) {
        chain.strongLinks.forEach { strongLink ->
            painter.line(
                from = rectanglesAndCoordinates.candidateScreenCoordinates(
                    strongLink.firstCell,
                    chain.symbol
                ),
                to = rectanglesAndCoordinates.candidateScreenCoordinates(
                    strongLink.secondCell,
                    chain.symbol
                ),
                bgColor = linkColor
            )
        }
    }
}
