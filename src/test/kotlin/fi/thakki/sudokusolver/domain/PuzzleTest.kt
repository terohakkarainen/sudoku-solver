package fi.thakki.sudokusolver.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isNull
import fi.thakki.sudokusolver.util.PuzzleBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTest {

    @Test
    fun `regionFuncs size must match dimension`() {
        assertThrows<IllegalArgumentException> {
            Puzzle(
                dimension = Dimension(1),
                regionFuncs = emptyList()
            )
        }
    }

    @Test
    fun `region size does not match dimension`() {
        assertThrows<IllegalStateException> {
            Puzzle(
                dimension = Dimension(2),
                regionFuncs = listOf(
                    { puzzle -> setOf(puzzle.cellAt(Coordinates(0, 0))) },
                    { puzzle -> setOf(puzzle.cellAt(Coordinates(1, 1))) }
                )
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
                )
            )
        }
    }

    @Test
    fun `cellAt() out of range`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()

        assertThrows<IndexOutOfBoundsException> {
            puzzle.cellAt(Coordinates(9, 9))
        }
    }

    @Test
    fun `cellAt() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()

        val cell = puzzle.cellAt(Coordinates(0, 0))
        assertThat(cell.value).isNull()
    }

    @Test
    fun `regionOf() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()

        val cell = puzzle.cellAt(Coordinates(0, 0))
        val region = puzzle.regionOf(cell)
        assertThat(region).contains(cell)
        assertThat(region).hasSize(4)
    }
}
