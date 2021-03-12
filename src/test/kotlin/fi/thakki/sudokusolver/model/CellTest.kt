package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.jupiter.api.Test

internal class CellTest {

    private val someSymbol = 'a'
    private val someOtherSymbol = 'b'

    @Test
    fun `cells with same coordinates are considered equal`() {
        val cell1 = Cell(Coordinates(1, 2), emptySet()).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }
        val cell2 = Cell(Coordinates(1, 2), emptySet()).apply {
            value = someOtherSymbol
            type = CellValueType.SETTABLE
        }

        assertThat(cell1).isEqualTo(cell2)
    }

    @Test
    fun `cells with different coordinates are considered unequal`() {
        val cell1 = Cell(Coordinates(2, 1), emptySet()).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }
        val cell2 = Cell(Coordinates(1, 2), emptySet()).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }

        assertThat(cell1).isNotEqualTo(cell2)
    }
}
