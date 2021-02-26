package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.jupiter.api.Test

internal class CellTest {

    @Test
    fun `cells with same coordinates are considered equal`() {
        val cell1 = Cell(Coordinates(1, 2), "a", CellValueType.GIVEN)
        val cell2 = Cell(Coordinates(1, 2), "b", CellValueType.SETTABLE)

        assertThat(cell1).isEqualTo(cell2)
    }

    @Test
    fun `cells with different coordinates are considered unequal`() {
        val cell1 = Cell(Coordinates(2, 1), "a", CellValueType.GIVEN)
        val cell2 = Cell(Coordinates(1, 2), "a", CellValueType.GIVEN)

        assertThat(cell1).isNotEqualTo(cell2)
    }
}
