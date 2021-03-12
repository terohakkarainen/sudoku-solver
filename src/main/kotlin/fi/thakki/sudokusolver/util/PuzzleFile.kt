package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.Symbols

class PuzzleFile {

    lateinit var dimension: Dimension
    lateinit var symbols: Symbols
    lateinit var givens: List<String> // Must be public for SnakeYaml to access it.

    fun getGivenCells(): Set<Cell> {
        var bandIndex = dimension.value - 1
        val result = mutableSetOf<Cell>()
        givens.forEach { line ->
            line.findAnyOf(symbols.map { it.toString() })?.let {
                result.addAll(
                    line.filter { character -> character in symbols || character == CELL_NOT_GIVEN_MARKER }
                        .mapIndexedNotNull { index, character ->
                            when (character) {
                                CELL_NOT_GIVEN_MARKER -> null
                                else -> {
                                    Cell(Coordinates(index, bandIndex), symbols).apply {
                                        setGiven(character)
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
