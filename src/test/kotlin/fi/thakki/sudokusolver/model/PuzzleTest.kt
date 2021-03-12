package fi.thakki.sudokusolver.model

import fi.thakki.sudokusolver.util.PuzzleTraverser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTest {

    private val someSymbol = 'a'
    private val someOtherSymbol = 'b'

    @Test
    fun `regionFuncs size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = setOf(someSymbol)
            )
        }
    }

    @Test
    fun `symbols size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = setOf(someSymbol, someOtherSymbol)
            )
        }
    }

    @Test
    fun `region size does not match dimension`() {
        assertThrows<IllegalStateException> {
            Puzzle(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { puzzle -> Region(setOf(PuzzleTraverser(puzzle).cellAt(Coordinates(0, 0)))) },
                    { puzzle -> Region(setOf(PuzzleTraverser(puzzle).cellAt(Coordinates(1, 1)))) }
                ),
                symbols = setOf(someSymbol, someOtherSymbol)
            )
        }
    }

    @Test
    fun `overlapping regions`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { puzzle -> Region(puzzle.bands.first().toSet()) },
                    { puzzle -> Region(puzzle.stacks.first().toSet()) }
                ),
                symbols = setOf(someSymbol, someOtherSymbol)
            )
        }
    }
}
