package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.canvas.Canvas
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.Painter
import fi.thakki.sudokusolver.canvas.Pixel
import fi.thakki.sudokusolver.canvas.Size
import fi.thakki.sudokusolver.model.Band
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

const val ANSI_ESC = "\u001B"

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

    @Suppress("unused")
    enum class TextColor(val code: String) {
        RED("$ANSI_ESC[31m"),
        GREEN("$ANSI_ESC[32m"),
        YELLOW("$ANSI_ESC[33m"),
        BLUE("$ANSI_ESC[34m"),
        PURPLE("$ANSI_ESC[35m"),
        CYAN("$ANSI_ESC[36m"),
        MAGENTA("$ANSI_ESC[35m"),
        WHITE("$ANSI_ESC[37m");

        companion object {
            const val RESET = "$ANSI_ESC[0m"
        }
    }

    private val puzzleTraverser = PuzzleTraverser(puzzle)
    private val cellRegions = puzzle.cells.map { cell -> cell to puzzleTraverser.regionOf(cell) }.toMap()
    private val borders = getCellBorders(puzzle)
    private val candidatesPerRow = sqrt(puzzle.dimension.value.toDouble()).roundToInt()
    private val cellWidth = 2 * candidatesPerRow + 1
    private val cellWidthBlank = " ".repeat(cellWidth)
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
        printPuzzleNew(highlightedSymbol)
        printPuzzleOld(highlightedSymbol)
    }

    private fun printPuzzleNew(highlightedSymbol: Symbol? = null) {
        val regionLayer = canvas.layers[2]
        val gridLayer = canvas.layers[3]

        canvas.painterForLayer(gridLayer)
            .let { gridPainter ->
                paintHorizontalRulers(gridPainter, listOf(0, canvas.size.height - 1), Color.DEFAULT)
                paintVerticalRulers(gridPainter, listOf(1, canvas.size.width - 1), Color.DEFAULT)
                paintGrid(gridPainter, Color.DEFAULT)
            }

        canvas.painterForLayer(regionLayer)
//            .apply { painterFgColor = Color.LIGHT_GRAY }
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
                            bgColor = if (i % 2 == 0) Color.DARK_GRAY else Color.LIGHT_GRAY
                        )
                    }
                    i++
                }
            }

        canvas.printToScreen()
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
            painter.line(
                from = cellBorderTopLeftScreenCoordinates(Coordinates(0, y)),
                to = cellBorderTopRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, y)),
                character = Pixel.HORIZ_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.line(
            from = cellBorderBottomLeftScreenCoordinates(Coordinates(0, 0)),
            to = cellBorderBottomRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, 0)),
            character = Pixel.HORIZ_LIGHT_LINE,
            fgColor = color
        )

        // Vertical lines
        (0 until puzzle.dimension.value).forEach { x ->
            painter.line(
                from = cellBorderBottomLeftScreenCoordinates(Coordinates(x, 0)),
                to = cellBorderTopLeftScreenCoordinates(Coordinates(x, puzzle.dimension.value - 1)),
                character = Pixel.VERT_LIGHT_LINE,
                fgColor = color
            )
        }
        painter.line(
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
                painter.line(
                    from = cellBorderTopLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderTopRightScreenCoordinates(cell.coordinates),
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.left) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.line(
                    from = cellBorderBottomLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderTopLeftScreenCoordinates(cell.coordinates),
                    character = Pixel.VERT_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.bottom) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.line(
                    from = cellBorderBottomLeftScreenCoordinates(cell.coordinates),
                    to = cellBorderBottomRightScreenCoordinates(cell.coordinates),
                    character = Pixel.HORIZ_HEAVY_LINE,
                    fgColor = color
                )
            else -> Unit
        }
        when (borders.right) {
            BorderType.PUZZLE, BorderType.REGION ->
                painter.line(
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

    fun printPuzzleOld(highlightedSymbol: Symbol? = null) {
        printHorizontalRuler()
        printHorizontalLine(
            puzzle.bands.last(), false
        )

        val rowsPerCell = ceil(puzzle.symbols.size.toDouble() / candidatesPerRow.toDouble()).toInt()
        val valueRowIndex = ceil(rowsPerCell.toDouble() / 2f).toInt() - 1

        puzzle.bands.reversed().forEachIndexed { index, band ->
            val bandIndex = puzzle.dimension.value - index - 1
            for (rowIndex in 0 until rowsPerCell) {
                printBand(band, bandIndex, rowIndex, rowIndex == valueRowIndex, highlightedSymbol)
            }
            printHorizontalLine(band, true)
        }

        printHorizontalRuler()
        println()
    }

    private fun printHorizontalRuler() {
        val initialOffset = " ".repeat(cellWidth / 2 + VERTICAL_RULER_OFFSET_LENGTH + 1)
        print(initialOffset + 0)
        for (i in 1 until puzzle.dimension.value) {
            print(cellWidthBlank + i)
        }
        println()
    }

    private fun printHorizontalLine(referenceBand: Band, useBottomBorder: Boolean) {
        print(VERTICAL_RULER_OFFSET + CROSS_SECTION)
        referenceBand.forEach { cell ->
            val cellBorders = checkNotNull(borders[cell])
            val borderProp = if (useBottomBorder) cellBorders::bottom else cellBorders::top
            when (borderProp.get()) {
                BorderType.PUZZLE, BorderType.REGION -> print(HORIZ_HEAVY_LINE.repeat(cellWidth))
                else -> print(HORIZ_LIGHT_LINE.repeat(cellWidth))
            }
            print(CROSS_SECTION)
        }
        println()
    }

    private fun printBand(
        band: Band,
        bandIndex: Int,
        rowIndex: Int,
        isValueRow: Boolean,
        highlightedSymbol: Symbol?
    ) {
        if (isValueRow) print(" $bandIndex ") else print(VERTICAL_RULER_OFFSET)

        band.forEach { cell ->
            val cellBorders = checkNotNull(borders[cell])
            when (cellBorders.left) {
                BorderType.PUZZLE, BorderType.REGION -> print(VERT_HEAVY_LINE)
                else -> print(VERT_LIGHT_LINE)
            }
            if (cell.hasValue()) {
                printCellWithValue(cell, isValueRow)
            } else {
                printCellWithoutValue(cell, rowIndex, highlightedSymbol)
            }
        }

        if (isValueRow) println("$VERT_HEAVY_LINE $bandIndex") else println(VERT_HEAVY_LINE)
    }

    private fun printCellWithValue(cell: Cell, isValueRow: Boolean) {
        if (isValueRow) {
            (" ".repeat((cellWidth) / 2)).let { padding ->
                padding + inColor(checkNotNull(cell.value), setCellColor(cell)) + padding
            }.run {
                print(this)
            }
        } else print(cellWidthBlank)
    }

    private fun printCellWithoutValue(
        cell: Cell,
        rowIndex: Int,
        highlightedSymbol: Symbol?
    ) {
        val allSymbolsList = puzzle.symbols.toList().sorted()
        val isCandidateBySymbol = allSymbolsList.map { symbol ->
            symbol to cell.analysis.candidates.contains(symbol)
        }.toMap()

        val candidatesInThisRow = allSymbolsList.chunked(candidatesPerRow)[rowIndex]
        val candidatesString =
            candidatesInThisRow.joinToString(separator = " ") { symbol ->
                if (isCandidateBySymbol.getOrDefault(symbol, false) &&
                    (highlightedSymbol == null || highlightedSymbol == symbol)
                ) {
                    inColor(symbol, candidateColor(cell, symbol))
                } else " "
            }

        if (candidatesInThisRow.size == candidatesPerRow) {
            print(" $candidatesString ")
        } else {
            val extraPadding = (candidatesPerRow - candidatesInThisRow.size) * 2
            print(" $candidatesString ".plus(" ".repeat(extraPadding)))
        }
    }

    private fun candidateColor(cell: Cell, candidate: Symbol): TextColor =
        cell.analysis.strongLinks.find { it.symbol == candidate }?.let {
            STRONG_LINKED_CANDIDATE_COLOR
        } ?: CANDIDATE_COLOR

    private fun setCellColor(cell: Cell): TextColor =
        when (cell.type) {
            CellValueType.GIVEN -> GIVEN_CELL_COLOR
            else -> SET_CELL_COLOR
        }

    private fun inColor(s: Symbol, color: TextColor): String =
        "${color.code}$s${TextColor.RESET}"

    companion object {
        private const val VERT_HEAVY_LINE = "\u2551"
        private const val VERT_LIGHT_LINE = "\u250a"
        private const val HORIZ_HEAVY_LINE = "\u2550"
        private const val HORIZ_LIGHT_LINE = "\u2508"
        private const val CROSS_SECTION = "\u253c"
        private const val VERTICAL_RULER_OFFSET_LENGTH = 3
        private val VERTICAL_RULER_OFFSET = " ".repeat(VERTICAL_RULER_OFFSET_LENGTH)
        private val SET_CELL_COLOR = TextColor.YELLOW
        private val GIVEN_CELL_COLOR = TextColor.GREEN
        private val CANDIDATE_COLOR = TextColor.CYAN
        private val STRONG_LINKED_CANDIDATE_COLOR = TextColor.RED
    }
}
