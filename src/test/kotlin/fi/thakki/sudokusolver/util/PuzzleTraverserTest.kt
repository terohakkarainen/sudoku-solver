package fi.thakki.sudokusolver.util

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTraverserTest {

    @Test
    fun `cellAt() out of range`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThrows<IndexOutOfBoundsException> {
            traverser.cellAt(Coordinates(9, 9))
        }
    }

    @Test
    fun `cellAt() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val cell = traverser.cellAt(Coordinates(0, 0))
        assertThat(cell.value).isNull()
    }

    @Test
    fun `bandOf() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = traverser.bandOf(Cell(Coordinates(0, 0)))

        assertThat(result).isSameAs(puzzle.bands.first())
    }

    @Test
    fun `bandOf() coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThrows<IndexOutOfBoundsException> {
            traverser.bandOf(Cell(Coordinates(10, 10)))
        }
    }

    @Test
    fun `stackOf() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = traverser.stackOf(Cell(Coordinates(0, 0)))

        assertThat(result).isSameAs(puzzle.stacks.first())
    }

    @Test
    fun `stackOf() coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThrows<IndexOutOfBoundsException> {
            traverser.stackOf(Cell(Coordinates(10, 10)))
        }
    }

    @Test
    fun `regionOf() happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val cell = traverser.cellAt(Coordinates(0, 0))
        val region = traverser.regionOf(cell)
        assertThat(region).contains(cell)
        assertThat(region).hasSize(4)
    }

    @Test
    fun `regionOf() coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThrows<NoSuchElementException> {
            traverser.regionOf(Cell(Coordinates(10, 10)))
        }
    }

    @Test
    fun `above() target cell exists`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = checkNotNull(traverser.above(Cell(Coordinates(0, 0))))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 1))
    }

    @Test
    fun `above() target cell does not exist`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.above(Cell(Coordinates(0, 3)))).isNull()
    }

    @Test
    fun `above() source cell coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.above(Cell(Coordinates(10, 10)))).isNull()
    }

    @Test
    fun `below() target cell exists`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = checkNotNull(traverser.below(Cell(Coordinates(0, 1))))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 0))
    }

    @Test
    fun `below() target cell does not exist`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.below(Cell(Coordinates(0, 0)))).isNull()
    }

    @Test
    fun `below() source cell coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.below(Cell(Coordinates(10, 10)))).isNull()
    }

    @Test
    fun `leftOf() target cell exists`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = checkNotNull(traverser.leftOf(Cell(Coordinates(1, 0))))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 0))
    }

    @Test
    fun `leftOf() target cell does not exist`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.leftOf(Cell(Coordinates(0, 0)))).isNull()
    }

    @Test
    fun `leftOf() source cell coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.leftOf(Cell(Coordinates(10, 10)))).isNull()
    }

    @Test
    fun `rightOf() target cell exists`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        val result = checkNotNull(traverser.rightOf(Cell(Coordinates(0, 0))))

        assertThat(result.coordinates).isEqualTo(Coordinates(1, 0))
    }

    @Test
    fun `rightOf() target cell does not exist`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.rightOf(Cell(Coordinates(3, 0)))).isNull()
    }

    @Test
    fun `rightOf() source cell coordinates out of bounds`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4).build()
        val traverser = PuzzleTraverser(puzzle)

        assertThat(traverser.rightOf(Cell(Coordinates(10, 10)))).isNull()
    }
}
