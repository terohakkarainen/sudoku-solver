package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Cells
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.Symbols

class PuzzleFile {

    lateinit var dimension: Dimension
    lateinit var symbols: Symbols
    lateinit var givens: List<String>

    fun getGivenCells(): Cells {
        var bandIndex = dimension.value - 1
        val result = mutableSetOf<Cell>()
        givens.forEach { line ->
            line.findAnyOf(symbols)?.let {
                result.addAll(
                    line.filter { c -> c.toString() in symbols || c == CELL_NOT_GIVEN_MARKER }
                        .mapIndexedNotNull { index, c ->
                            when (c) {
                                CELL_NOT_GIVEN_MARKER -> null
                                else -> {
                                    Cell(Coordinates(index, bandIndex)).apply {
                                        setGiven(c.toString())
                                    }
                                }
                            }
                        }
                )
                bandIndex--
            }
        }
        return result
    }

    companion object {
        private const val CELL_NOT_GIVEN_MARKER = '.'
    }
}
