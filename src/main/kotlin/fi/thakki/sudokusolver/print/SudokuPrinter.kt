package fi.thakki.sudokusolver.print

import fi.thakki.sudokusolver.canvas.Canvas
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.Painter
import fi.thakki.sudokusolver.canvas.PixelValue
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkChain
import fi.thakki.sudokusolver.model.Symbol

@Suppress("TooManyFunctions")
class SudokuPrinter(private val puzzle: Puzzle) {

    private val rectanglesAndCoordinates = RectanglesAndCoordinates(puzzle)

    @Suppress("MagicNumber")
    fun printPuzzle(highlightedSymbol: Symbol? = null) {
        val numberOfLayersNeeded = highlightedSymbol?.let { 4 } ?: 3
        val canvas = Canvas(rectanglesAndCoordinates.canvasSize, numberOfLayersNeeded)

        canvas.layers.reversed().iterator().let { layerReverseIterator ->
            paintGridLayer(canvas.painterForLayer(layerReverseIterator.next()))
            paintRegionLayer(canvas.painterForLayer(layerReverseIterator.next()))
            paintValueLayer(canvas.painterForLayer(layerReverseIterator.next()))
            highlightedSymbol?.let { symbol ->
                paintHighlightLayer(canvas.painterForLayer(layerReverseIterator.next()), symbol)
            }
        }

        canvas.copyToScreen()
    }

    private fun paintGridLayer(painter: Painter) {
        painter.layer.size.let { layerSize ->
            paintHorizontalRulers(painter, listOf(0, layerSize.height - 1))
            paintVerticalRulers(painter, listOf(1, layerSize.width - 1))
            paintGrid(painter)
        }
    }

    private fun paintRegionLayer(painter: Painter) {
        CellBorders(puzzle).getCellBorders().entries.forEach { entry ->
            paintCellBorders(painter, entry.key, entry.value)
        }

        fun isEven(i: Int): Boolean = i % 2 == 0

        var i = 0
        puzzle.regions.forEach { region ->
            paintRegion(painter, region, if (isEven(i)) REGION_COLOR1 else REGION_COLOR2)
            i++
        }
    }

    private fun paintValueLayer(painter: Painter) {
        puzzle.cells.forEach { cell ->
            if (cell.hasValue()) {
                paintCellWithValue(painter, cell)
            } else {
                paintCellWithoutValue(painter, cell)
            }
        }
    }

    private fun paintHighlightLayer(painter: Painter, symbol: Symbol) {
        puzzle.cells.cellsWithoutValue().forEach { cell ->
            paintCellWithSingleCandidate(painter, cell, symbol)
        }
        puzzle.allCellCollections()
            .flatMap { it.analysis.strongLinks }
            .filter { it.symbol == symbol }
            .forEach { strongLink ->
                paintStrongLink(painter, strongLink)
            }
        puzzle.analysis.strongLinkChains
            .filter { it.symbol == symbol }
            .forEach { linkChain ->
                paintStrongLinkChain(painter, linkChain)
            }
    }

    private fun paintHorizontalRulers(painter: Painter, rows: List<Coordinate>) {
        rows.forEach { y ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = rectanglesAndCoordinates.cellMiddleScreenCoordinates(Coordinates(index, 0))
                painter.pixel(
                    PixelValue.Character(index.toString().toCharArray().single()),
                    Coordinates(cellMiddle.x, y),
                    RULER_COLOR
                )
            }
        }
    }

    private fun paintVerticalRulers(painter: Painter, columns: List<Coordinate>) {
        columns.forEach { x ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = rectanglesAndCoordinates.cellMiddleScreenCoordinates(Coordinates(0, index))
                painter.pixel(
                    PixelValue.Character(index.toString().toCharArray().single()),
                    Coordinates(x, cellMiddle.y),
                    RULER_COLOR
                )
            }
        }
    }

    private fun paintGrid(painter: Painter) {
        val color = GRID_COLOR
        (0 until puzzle.dimension.value).forEach { index ->
            val borderRectangle = rectanglesAndCoordinates.cellBorderRectangle(Coordinates(index, index))
            horizontalGridLine(painter, borderRectangle.topLeft.y, color)
            verticalGridLine(painter, borderRectangle.topLeft.x, color)
            when (index) {
                0 -> horizontalGridLine(painter, borderRectangle.bottomLeft.y, color)
                puzzle.dimension.value - 1 -> verticalGridLine(painter, borderRectangle.topRight.x, color)
            }
        }
    }

    private fun horizontalGridLine(painter: Painter, y: Coordinate, color: Color) {
        painter.perpendicularLine(
            from = Coordinates(RectanglesAndCoordinates.VERTICAL_RULER_OFFSET_LENGTH, y),
            to = Coordinates(painter.layer.size.width - RectanglesAndCoordinates.VERTICAL_RULER_OFFSET_LENGTH, y),
            value = PixelValue.Border(PixelValue.Border.HORIZ_LIGHT_LINE),
            fgColor = color
        )
    }

    private fun verticalGridLine(painter: Painter, x: Coordinate, color: Color) {
        painter.perpendicularLine(
            from = Coordinates(x, 1),
            to = Coordinates(x, painter.layer.size.height - 2),
            value = PixelValue.Border(PixelValue.Border.VERT_LIGHT_LINE),
            fgColor = color
        )
    }

    private fun paintCellBorders(painter: Painter, cell: Cell, borders: Borders) {
        val color = BORDER_COLOR
        rectanglesAndCoordinates.cellBorderRectangle(cell).let { borderRectangle ->
            paintBorder(painter, borders.top, borderRectangle.topLeft, borderRectangle.topRight, color)
            paintBorder(painter, borders.left, borderRectangle.bottomLeft, borderRectangle.topLeft, color)
            paintBorder(painter, borders.bottom, borderRectangle.bottomLeft, borderRectangle.bottomRight, color)
            paintBorder(painter, borders.right, borderRectangle.bottomRight, borderRectangle.topRight, color)
        }
    }

    private fun paintBorder(
        painter: Painter,
        borderType: BorderType,
        from: Coordinates,
        to: Coordinates,
        color: Color
    ) {
        when (borderType) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.perpendicularLine(
                    from = from,
                    to = to,
                    value = if (from.y == to.y) PixelValue.Border(PixelValue.Border.HORIZ_HEAVY_LINE)
                    else PixelValue.Border(PixelValue.Border.VERT_HEAVY_LINE),
                    fgColor = color
                )
            else -> Unit
        }
    }

    private fun paintRegion(painter: Painter, region: Region, bgColor: Color) {
        region.cells.forEach { cell ->
            painter.filledRectangle(
                rectangle = rectanglesAndCoordinates.cellRectangle(cell),
                bgColor = bgColor
            )
        }
    }

    private fun paintCellWithValue(painter: Painter, cell: Cell) {
        painter.pixel(
            cell.value?.let { PixelValue.Character(it) },
            rectanglesAndCoordinates.cellMiddleScreenCoordinates(cell.coordinates),
            if (cell.type == CellValueType.GIVEN) GIVEN_VALUE_COLOR else SET_VALUE_COLOR
        )
    }

    private fun paintCellWithoutValue(painter: Painter, cell: Cell) {
        puzzle.symbols.sorted().forEach { symbol ->
            if (cell.analysis.candidates.contains(symbol)) {
                painter.pixel(
                    PixelValue.Character(symbol),
                    rectanglesAndCoordinates.candidateScreenCoordinates(cell, symbol),
                    CANDIDATE_COLOR
                )
            }
        }
    }

    private fun paintCellWithSingleCandidate(painter: Painter, cell: Cell, candidate: Symbol) {
        painter.characterRectangle(
            rectanglesAndCoordinates.cellRectangle(cell),
            PixelValue.NO_VALUE
        )
        if (cell.analysis.candidates.contains(candidate)) {
            painter.pixel(
                PixelValue.Character(candidate),
                rectanglesAndCoordinates.candidateScreenCoordinates(cell, candidate),
                CANDIDATE_COLOR
            )
        }
    }

    private fun paintStrongLink(painter: Painter, strongLink: StrongLink) {
        painter.freeFormLine(
            from = rectanglesAndCoordinates.candidateScreenCoordinates(
                strongLink.firstCell,
                strongLink.symbol
            ),
            to = rectanglesAndCoordinates.candidateScreenCoordinates(
                strongLink.secondCell,
                strongLink.symbol
            ),
            bgColor = STRONG_LINK_COLOR
        )
    }

    private fun paintStrongLinkChain(painter: Painter, chain: StrongLinkChain) {
        chain.strongLinks.forEach { strongLink ->
            painter.freeFormLine(
                from = rectanglesAndCoordinates.candidateScreenCoordinates(
                    strongLink.firstCell,
                    chain.symbol
                ),
                to = rectanglesAndCoordinates.candidateScreenCoordinates(
                    strongLink.secondCell,
                    chain.symbol
                ),
                bgColor = STRONG_LINK_CHAIN_COLOR
            )
        }
    }

    companion object {
        private val RULER_COLOR = Color.DEFAULT
        private val GRID_COLOR = Color.DEFAULT
        private val BORDER_COLOR = Color.BLUE
        private val REGION_COLOR1 = Color.DARK_GRAY
        private val REGION_COLOR2 = Color.BLACK
        private val GIVEN_VALUE_COLOR = Color.BLUE
        private val SET_VALUE_COLOR = Color.LIGHT_GREEN
        private val CANDIDATE_COLOR = Color.DEFAULT
        private val STRONG_LINK_COLOR = Color.LIGHT_YELLOW
        private val STRONG_LINK_CHAIN_COLOR = Color.LIGHT_MAGENTA
    }
}
