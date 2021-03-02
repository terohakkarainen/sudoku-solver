package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.model.Band
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
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
    private val cellRegions = puzzle.cells.map { cell -> cell to puzzleTraverser.regionOf(cell) }.toMap()
    private val borders = getCellBorders(puzzle)

    private fun getCellBorders(puzzle: Puzzle): Map<Cell, Borders> =
        puzzle.cells.map { cell ->
            cell to Borders(
                top = when {
                    isEdgeCell(cell) -> BorderType.PUZZLE
                    inDifferentRegions(cell, puzzleTraverser.above(cell)) -> BorderType.REGION
                    else -> BorderType.CELL
                },
                bottom = when {
                    isEdgeCell(cell) -> BorderType.PUZZLE
                    inDifferentRegions(cell, puzzleTraverser.below(cell)) -> BorderType.REGION
                    else -> BorderType.CELL
                },
                left = when {
                    isEdgeCell(cell) -> BorderType.PUZZLE
                    inDifferentRegions(cell, puzzleTraverser.leftOf(cell)) -> BorderType.REGION
                    else -> BorderType.CELL
                },
                right = when {
                    isEdgeCell(cell) -> BorderType.PUZZLE
                    inDifferentRegions(cell, puzzleTraverser.rightOf(cell)) -> BorderType.REGION
                    else -> BorderType.CELL
                }
            )
        }.toMap()

    private fun isEdgeCell(cell: Cell): Boolean =
        (puzzle.dimension.value - 1).let { maxCoordinate ->
            cell.coordinates.x == 0 || cell.coordinates.y == 0 ||
                    cell.coordinates.x == maxCoordinate || cell.coordinates.y == maxCoordinate
        }

    private fun inDifferentRegions(cell1: Cell, cell2: Cell?): Boolean =
        cell2?.let {
            cellRegions[cell1] != cellRegions[cell2]
        } ?: false

    fun printPuzzle() {
        printHorizontalRuler()
        printHorizontalLine(puzzle.bands.last(), false)

        puzzle.bands.reversed().forEachIndexed { index, band ->
            val bandIndex = puzzle.dimension.value - index - 1
            printBand(band, bandIndex, false)
            printBand(band, bandIndex, true)
            printBand(band, bandIndex, false)
            printHorizontalLine(band, true)
        }

        printHorizontalRuler()
        println()
    }

    private fun printHorizontalRuler() {
        val initialOffset = " ".repeat(CELL_WIDTH / 2 + VERTICAL_RULER_OFFSET_LENGTH + 1)
        print(initialOffset + 0)
        for (i in 1 until puzzle.dimension.value) {
            print(" ".repeat(CELL_WIDTH) + i)
        }
        println()
    }

    private fun printHorizontalLine(referenceBand: Band, useBottomBorder: Boolean) {
        print(VERTICAL_RULER_OFFSET + CROSS_SECTION)
        referenceBand.forEach { cell ->
            val cellBorders = checkNotNull(borders[cell])
            val borderProp = if (useBottomBorder) cellBorders::bottom else cellBorders::top
            when (borderProp.get()) {
                BorderType.PUZZLE, BorderType.REGION -> print(HORIZ_HEAVY_LINE.repeat(CELL_WIDTH))
                else -> print(HORIZ_LIGHT_LINE.repeat(CELL_WIDTH))
            }
            print(CROSS_SECTION)
        }
        println()
    }

    private fun printBand(band: Band, index: Int, printValue: Boolean) {
        if (printValue) print(" $index ") else print(VERTICAL_RULER_OFFSET)
        band.forEach { cell ->
            val cellBorders = checkNotNull(borders[cell])
            when (cellBorders.left) {
                BorderType.PUZZLE, BorderType.REGION -> print(VERT_HEAVY_LINE)
                else -> print(VERT_LIGHT_LINE)
            }
            if (printValue) {
                val pad = " ".repeat((CELL_WIDTH) / 2)
                print(pad + (cell.value ?: ".") + pad)
            } else print(" ".repeat(CELL_WIDTH))
        }
        if (printValue) println("$VERT_HEAVY_LINE $index") else println(VERT_HEAVY_LINE)
    }

    companion object {
        private const val VERT_HEAVY_LINE = "\u2503"
        private const val VERT_LIGHT_LINE = "\u250a"
        private const val HORIZ_HEAVY_LINE = "\u2501"
        private const val HORIZ_LIGHT_LINE = "\u2508"
        private const val CROSS_SECTION = "\u253c"
        private const val CELL_WIDTH = 9
        private const val VERTICAL_RULER_OFFSET_LENGTH = 3
        private val VERTICAL_RULER_OFFSET = " ".repeat(VERTICAL_RULER_OFFSET_LENGTH)
    }
}
