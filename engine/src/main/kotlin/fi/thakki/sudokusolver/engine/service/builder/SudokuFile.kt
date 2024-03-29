package fi.thakki.sudokusolver.engine.service.builder

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.CellCoordinates
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Dimension
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.model.Symbols

class SudokuFile {

    // All fields must be public for SnakeYaml to access it.
    lateinit var dimension: Dimension
    lateinit var symbols: Set<Symbol>
    lateinit var givens: List<String>
    lateinit var regions: List<Map<Int, List<String>>>

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

    fun hasCustomRegions(): Boolean =
        this::regions.isInitialized

    fun getCoordinatesForRegions(): Set<CellCoordinates> =
        regions.map { region ->
            region.values.single().map { coordinatesString ->
                toCoordinates(coordinatesString)
            }.toSet()
        }.toSet()

    private fun toCoordinates(input: String): Coordinates {
        coordinatesPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            return Coordinates(x.toInt(), y.toInt())
        } ?: throw IllegalArgumentException("Unable to parse coordinates from $input")
    }

    companion object {
        private const val CELL_NOT_GIVEN_MARKER = '.'
        private val coordinatesPattern = Regex("^([0-9]*),([0-9]*)$")
    }
}
