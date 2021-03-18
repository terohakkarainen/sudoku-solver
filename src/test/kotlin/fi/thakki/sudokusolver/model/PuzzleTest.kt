package fi.thakki.sudokusolver.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTest {

    private val someSymbol = 'a'
    private val someOtherSymbol = 'b'

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
                    { cells -> Region(setOf(cells.first())) },
                    { cells -> Region(setOf(cells.last())) }
                ),
                symbols = Symbols(someSymbol, someOtherSymbol)
            )
        }
    }

    @Test
    fun `overlapping regions`() {
        assertThrows<IllegalArgumentException> {
            Puzzle.of(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { cells -> Region(cells.filter { it.coordinates.x == 0 }.toSet()) },
                    { cells -> Region(cells.filter { it.coordinates.y == 0 }.toSet()) }
                ),
                symbols = Symbols(someSymbol, someOtherSymbol)
            )
        }
    }
}
