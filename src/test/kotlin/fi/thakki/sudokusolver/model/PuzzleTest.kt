package fi.thakki.sudokusolver.model

import fi.thakki.sudokusolver.util.PuzzleTraverser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTest {

    @Test
    fun `regionFuncs size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = setOf("a")
            )
        }
    }

    @Test
    fun `symbols size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(1),
                regionFuncs = emptyList(),
                symbols = setOf("a", "b")
            )
        }
    }

    @Test
    fun `region size does not match dimension`() {
        assertThrows<IllegalStateException> {
            Puzzle(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { puzzle -> setOf(PuzzleTraverser(puzzle).cellAt(Coordinates(0, 0))) },
                    { puzzle -> setOf(PuzzleTraverser(puzzle).cellAt(Coordinates(1, 1))) }
                ),
                symbols = setOf("a", "b")
            )
        }
    }

    @Test
    fun `overlapping regions`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { puzzle -> puzzle.bands.first().toSet() },
                    { puzzle -> puzzle.stacks.first().toSet() }
                ),
                symbols = setOf("a", "b")
            )
        }
    }
}
