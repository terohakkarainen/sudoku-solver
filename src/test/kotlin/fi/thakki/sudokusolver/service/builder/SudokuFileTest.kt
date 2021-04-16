package fi.thakki.sudokusolver.service.builder

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Symbol
import org.junit.jupiter.api.Test

internal class SudokuFileTest {

    @Test
    fun `givens can contain lines with no given cells`() {
        val fileUnderTest = SudokuFile().apply {
            dimension = StandardDimensions.DIMENSION_4
            symbols = StandardSymbols.SYMBOLS_14
            givens = listOf(
                "+-----------+",
                "| 1 . | . . |",
                "| . . | . 3 |",
                "+-----------+",
                "| . . | . . |",
                "| . 2 | . . |",
                "+-----------+"
            )
        }

        val givenCells = fileUnderTest.getGivenCells()
        assertThat(givenCells).hasSize(3)
        assertCellWithValueIsInCoordinates(givenCells, '2', Coordinates(1, 0))
        assertCellWithValueIsInCoordinates(givenCells, '1', Coordinates(0, 3))
        assertCellWithValueIsInCoordinates(givenCells, '3', Coordinates(3, 2))
    }

    private fun assertCellWithValueIsInCoordinates(cells: Set<Cell>, value: Symbol, expectedCoordinates: Coordinates) {
        assertThat(cells.single { it.value == value }.coordinates).isEqualTo(expectedCoordinates)
    }
}
