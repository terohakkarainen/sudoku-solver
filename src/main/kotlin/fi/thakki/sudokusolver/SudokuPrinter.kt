package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.model.Band
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.util.PuzzleTraverser
import kotlin.math.ceil
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
    private val candidatesPerRow = sqrt(puzzle.dimension.value.toDouble()).toInt()
    private val cellWidth = 2 * candidatesPerRow + 1
    private val cellWidthBlank = " ".repeat(cellWidth)

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

    fun printPuzzle() {
        printHorizontalRuler()
        printHorizontalLine(puzzle.bands.last(), false)

        val rowsPerCell = ceil(puzzle.symbols.size.toDouble() / candidatesPerRow.toDouble()).toInt()
        val valueRowIndex = ceil(rowsPerCell.toDouble() / 2f).toInt() - 1

        puzzle.bands.reversed().forEachIndexed { index, band ->
            val bandIndex = puzzle.dimension.value - index - 1
            for (rowIndex in 0 until rowsPerCell) {
                printBand(band, bandIndex, rowIndex, rowIndex == valueRowIndex)
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

    private fun printBand(band: Band, bandIndex: Int, rowIndex: Int, isValueRow: Boolean) {
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
                printCellWithoutValue(cell, rowIndex)
            }
        }

        if (isValueRow) println("$VERT_HEAVY_LINE $bandIndex") else println(VERT_HEAVY_LINE)
    }

    private fun printCellWithValue(cell: Cell, isValueRow: Boolean) {
        if (isValueRow) {
            (" ".repeat((cellWidth) / 2 - 1)).let { padding ->
                DOT + padding + cell.value + padding + DOT
            }.run {
                print(this)
            }
        } else print(cellWidthBlank)
    }

    private fun printCellWithoutValue(cell: Cell, rowIndex: Int) {
        val allSymbolsList = puzzle.symbols.toList().sorted()
        val symbolToIsCandidate = allSymbolsList.map { symbol ->
            symbol to cell.analysis.candidates.contains(symbol)
        }.toMap()

        val candidatesString =
            allSymbolsList.chunked(candidatesPerRow)[rowIndex]
                .joinToString(separator = " ") { symbol ->
                    if (true == symbolToIsCandidate[symbol]) {
                        symbol
                    } else " "
                }
        print(" $candidatesString ")
    }

    companion object {
        private const val VERT_HEAVY_LINE = "\u2503"
        private const val VERT_LIGHT_LINE = "\u250a"
        private const val HORIZ_HEAVY_LINE = "\u2501"
        private const val HORIZ_LIGHT_LINE = "\u2508"
        private const val CROSS_SECTION = "\u253c"
        private const val DOT = "\u2022"
        private const val VERTICAL_RULER_OFFSET_LENGTH = 3
        private val VERTICAL_RULER_OFFSET = " ".repeat(VERTICAL_RULER_OFFSET_LENGTH)
    }
}
