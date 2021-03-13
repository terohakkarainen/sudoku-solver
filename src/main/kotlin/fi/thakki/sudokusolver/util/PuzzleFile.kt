package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.model.Symbols

class PuzzleFile {

    lateinit var dimension: Dimension
    lateinit var symbols: Set<Symbol>
    lateinit var givens: List<String> // Must be public for SnakeYaml to access it.

    fun getGivenCells(): Set<Cell> {
        var bandIndex = dimension.value - 1
        val result = mutableSetOf<Cell>()
        val processedChars = symbols.plus(CELL_NOT_GIVEN_MARKER)
        givens.forEach { line ->
            line.findAnyOf(processedChars.map { it.toString() })?.let {
                result.addAll(
                    line.filter { character -> character in processedChars }
                        .mapIndexedNotNull { index, character ->
                            when (character) {
                                CELL_NOT_GIVEN_MARKER -> null
                                else -> {
                                    Cell(Coordinates(index, bandIndex), Symbols(symbols)).apply {
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
