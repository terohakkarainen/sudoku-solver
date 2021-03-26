package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.hasMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTest {

    private val someSymbol = 'a'
    private val someOtherSymbol = 'b'
    private val someThirdSymbol = 'c'

    @Test
    fun `regionFuncs size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle.of(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = Symbols(someSymbol)
            )
        }
    }

    @Test
    fun `symbols size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle.of(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = Symbols(someSymbol, someOtherSymbol)
            )
        }
    }

    @Test
    fun `region size does not match dimension`() {
        assertThrows<IllegalStateException> {
            Puzzle.of(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { cells -> setOf(cells.first()).map { it.coordinates }.toSet() },
                    { cells -> setOf(cells.last()).map { it.coordinates }.toSet() }
                ),
                symbols = Symbols(someSymbol, someOtherSymbol)
            )
        }
    }

    @Test
    fun `there can be no overlapping regions`() {
        assertThat(
            assertThrows<IllegalStateException> {
                Puzzle.of(
                    dimension = Dimension(2),
                    regionFuncs = listOf(
                        { cells -> cells.map { it.coordinates }.filter { it.x == 0 }.toSet() },
                        { cells -> cells.map { it.coordinates }.filter { it.y == 0 }.toSet() }
                    ),
                    symbols = Symbols(someSymbol, someOtherSymbol)
                )
            }
        ).hasMessage("Cell (0,0) belongs to multiple regions or no region")
    }

    @Test
    fun `there can be only directly adjacent cells in a region`() {
        val dimension = Dimension(3)
        val symbols = Symbols(someSymbol, someOtherSymbol, someThirdSymbol)

        // Region configuration looks like this:
        // 001
        // 110
        // 222
        assertThat(
            assertThrows<IllegalStateException> {
                Puzzle(
                    dimension = dimension,
                    symbols = symbols,
                    cells = Puzzle.cellsForDimension(dimension, symbols),
                    coordinatesForRegions = setOf(
                        setOf(Coordinates(0, 2), Coordinates(1, 2), Coordinates(2, 1)),
                        setOf(Coordinates(0, 1), Coordinates(1, 1), Coordinates(2, 2)),
                        setOf(Coordinates(0, 0), Coordinates(1, 0), Coordinates(2, 0))
                    )
                )
            }
        ).hasMessage("Cell (2,1) is not adjacent to any other cell in region")
    }
}
