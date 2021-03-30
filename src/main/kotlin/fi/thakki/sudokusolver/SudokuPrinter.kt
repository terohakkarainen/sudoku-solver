package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.canvas.Canvas
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.Painter
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
            VERTICAL_RULER_OFFSET_LENGTH * 2 +
                    puzzle.dimension.value * cellWidth +
                    puzzle.dimension.value,
            puzzle.dimension.value * cellHeight + puzzle.dimension.value + 3
        ).let { puzzleSize ->
            Canvas(
                size = puzzleSize,
                numberOfLayers = 3
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
        println("cellHeight is $cellHeight")
        println("cellWidth is $cellWidth")
        printPuzzleNew(highlightedSymbol)
        printPuzzleOld(highlightedSymbol)
    }

    fun printPuzzleNew(highlightedSymbol: Symbol? = null) {
        val linkLayer = canvas.layers[0]
        val valueLayer = canvas.layers[1]
        val gridLayer = canvas.layers[2]

        canvas.painterForLayer(gridLayer)
            .apply { painterFgColor = Color.DEFAULT }
            .let { borderPainter ->
                printHorizontalRulers(borderPainter, listOf(0, canvas.size.y - 1))
                printVerticalRulers(borderPainter, listOf(1, canvas.size.x - 1))
                printGrid(borderPainter)
            }

        canvas.painterForLayer(valueLayer).let { valuePainter ->
//            valuePainter.draw('1', Coordinates(0, 0), Color.CYAN)
        }

        canvas.painterForLayer(linkLayer).let { linkPainter ->
//            linkPainter.draw('1', Coordinates(0, 0), Color.RED)
        }
        canvas.printToScreen()
    }

    private fun printHorizontalRulers(painter: Painter, rows: List<Coordinate>) {
        rows.forEach { y ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = cellMiddleScreenCoordinates(Coordinates(index, 0))
                painter.draw(
                    index.toString(),
                    Coordinates(cellMiddle.x, y)
                )
            }
        }
    }

    private fun printVerticalRulers(painter: Painter, columns: List<Coordinate>) {
        columns.forEach { x ->
            (0 until puzzle.dimension.value).forEach { index ->
                val cellMiddle = cellMiddleScreenCoordinates(Coordinates(0, index))
                painter.draw(
                    index.toString(),
                    Coordinates(x, cellMiddle.y)
                )
            }
        }
    }

    private fun printGrid(painter: Painter) {
        // Horizontal lines
        (0 until puzzle.dimension.value).forEach { y ->
            painter.line(
                from = cellBorderTopLeftScreenCoordinates(Coordinates(0, y)),
                to = cellBorderTopRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, y)),
                character = HORIZ_LIGHT_LINE
            )
        }
        painter.line(
            from = cellBorderBottomLeftScreenCoordinates(Coordinates(0, 0)),
            to = cellBorderBottomRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, 0)),
            character = HORIZ_LIGHT_LINE
        )

        // Vertical lines
        (0 until puzzle.dimension.value).forEach { x ->
            painter.line(
                from = cellBorderBottomLeftScreenCoordinates(Coordinates(x, 0)),
                to = cellBorderTopLeftScreenCoordinates(Coordinates(x, puzzle.dimension.value - 1)),
                character = VERT_LIGHT_LINE
            )
        }
        painter.line(
            from = cellBorderBottomRightScreenCoordinates(Coordinates(puzzle.dimension.value - 1, 0)),
            to = cellBorderTopRightScreenCoordinates(
                Coordinates(puzzle.dimension.value - 1, puzzle.dimension.value - 1)
            ),
            character = VERT_LIGHT_LINE
        )
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

    private fun cellBorderTopRightScreenCoordinates(coordinates: Coordinates): Coordinates =
        Coordinates(
            x = coordinates.x * (cellWidth + 1) + cellWidth + VERTICAL_RULER_OFFSET_LENGTH + 1,
            y = coordinates.y * (cellHeight + 1) + cellHeight + 2
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
