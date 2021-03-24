package fi.thakki.sudokusolver.util

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import fi.thakki.sudokusolver.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinate
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Symbols
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleTraverserTest {

    private val messageBroker = ConsoleApplicationMessageBroker
    private val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_4X4, messageBroker).build()
    private val traverser = PuzzleTraverser(puzzle)

    @Test
    fun `cellAt() out of range`() {
        assertThrows<IndexOutOfBoundsException> {
            traverser.cellAt(Coordinates(9, 9))
        }
    }

    @Test
    fun `cellAt() happy case`() {
        val cell = traverser.cellAt(Coordinates(0, 0))
        assertThat(cell.value).isNull()
    }

    @Test
    fun `bandOf() happy case`() {
        val result = traverser.bandOf(newCell(0, 0))

        assertThat(result).isSameAs(puzzle.bands.first())
    }

    @Test
    fun `bandOf() coordinates out of bounds`() {
        assertThrows<IndexOutOfBoundsException> {
            traverser.bandOf(newCell(10, 10))
        }
    }

    @Test
    fun `stackOf() happy case`() {
        val result = traverser.stackOf(newCell(0, 0))

        assertThat(result).isSameAs(puzzle.stacks.first())
    }

    @Test
    fun `stackOf() coordinates out of bounds`() {
        assertThrows<IndexOutOfBoundsException> {
            traverser.stackOf(newCell(10, 10))
        }
    }

    @Test
    fun `regionOf() happy case`() {
        val cell = traverser.cellAt(Coordinates(0, 0))
        val region = traverser.regionOf(cell)
        assertThat(region).contains(cell)
        assertThat(region).hasSize(4)
    }

    @Test
    fun `regionOf() coordinates out of bounds`() {
        assertThrows<NoSuchElementException> {
            traverser.regionOf(newCell(10, 10))
        }
    }

    @Test
    fun `above() target cell exists`() {
        val result = checkNotNull(traverser.above(newCell(0, 0)))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 1))
    }

    @Test
    fun `above() target cell does not exist`() {
        assertThat(traverser.above(newCell(0, 3))).isNull()
    }

    @Test
    fun `above() source cell coordinates out of bounds`() {
        assertThat(traverser.above(newCell(10, 10))).isNull()
    }

    @Test
    fun `below() target cell exists`() {
        val result = checkNotNull(traverser.below(newCell(0, 1)))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 0))
    }

    @Test
    fun `below() target cell does not exist`() {
        assertThat(traverser.below(newCell(0, 0))).isNull()
    }

    @Test
    fun `below() source cell coordinates out of bounds`() {
        assertThat(traverser.below(newCell(10, 10))).isNull()
    }

    @Test
    fun `leftOf() target cell exists`() {
        val result = checkNotNull(traverser.leftOf(newCell(1, 0)))

        assertThat(result.coordinates).isEqualTo(Coordinates(0, 0))
    }

    @Test
    fun `leftOf() target cell does not exist`() {
        assertThat(traverser.leftOf(newCell(0, 0))).isNull()
    }

    @Test
    fun `leftOf() source cell coordinates out of bounds`() {
        assertThat(traverser.leftOf(newCell(10, 10))).isNull()
    }

    @Test
    fun `rightOf() target cell exists`() {
        val result = checkNotNull(traverser.rightOf(newCell(0, 0)))

        assertThat(result.coordinates).isEqualTo(Coordinates(1, 0))
    }

    @Test
    fun `rightOf() target cell does not exist`() {
        assertThat(traverser.rightOf(newCell(3, 0))).isNull()
    }

    @Test
    fun `rightOf() source cell coordinates out of bounds`() {
        assertThat(traverser.rightOf(newCell(10, 10))).isNull()
    }

    @Test
    fun `intersectionsOf() cells in same stack`() {
        val cell1 = newCell(2, 3)
        val cell2 = newCell(2, 0)
        val result = traverser.intersectionsOf(cell1, cell2)
        assertThat(result.first).isEqualTo(cell2)
        assertThat(result.second).isEqualTo(cell1)
    }

    @Test
    fun `intersectionsOf() cells in same band`() {
        val cell1 = newCell(3, 2)
        val cell2 = newCell(0, 2)
        val result = traverser.intersectionsOf(cell1, cell2)
        assertThat(result.first).isEqualTo(cell1)
        assertThat(result.second).isEqualTo(cell2)
    }

    @Test
    fun `intersectionsOf() cells not in same band or stack`() {
        val cell1 = newCell(1, 2)
        val cell2 = newCell(3, 3)
        val result = traverser.intersectionsOf(cell1, cell2)
        assertThat(result.first.coordinates).isEqualTo(Coordinates(1, 3))
        assertThat(result.second.coordinates).isEqualTo(Coordinates(3, 2))
    }

    private fun newCell(x: Coordinate, y: Coordinate): Cell =
        Cell(Coordinates(x, y), Symbols(emptySet()))
}
