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
                    { cells -> setOf(cells.first()).map { it.coordinates }.toSet() },
                    { cells -> setOf(cells.last()).map { it.coordinates }.toSet() }
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
                    { cells -> cells.map { it.coordinates }.filter { it.x == 0 }.toSet() },
                    { cells -> cells.map { it.coordinates }.filter { it.y == 0 }.toSet() }
                ),
                symbols = Symbols(someSymbol, someOtherSymbol)
            )
        }
    }
}
