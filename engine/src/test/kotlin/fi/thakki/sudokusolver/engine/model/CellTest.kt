package fi.thakki.sudokusolver.engine.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.jupiter.api.Test

internal class CellTest {

    private val someSymbol = 'a'
    private val someOtherSymbol = 'b'

    @Test
    fun `cells with same coordinates are considered equal`() {
        val cell1 = newCell(Coordinates(1, 2)).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }
        val cell2 = newCell(Coordinates(1, 2)).apply {
            value = someOtherSymbol
            type = CellValueType.SETTABLE
        }

        assertThat(cell1).isEqualTo(cell2)
    }

    @Test
    fun `cells with different coordinates are considered unequal`() {
        val cell1 = newCell(Coordinates(2, 1)).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }
        val cell2 = newCell(Coordinates(1, 2)).apply {
            value = someSymbol
            type = CellValueType.GIVEN
        }

        assertThat(cell1).isNotEqualTo(cell2)
    }

    private fun newCell(coordinates: Coordinates) =
        Cell(coordinates, Symbols((emptySet())))
}
