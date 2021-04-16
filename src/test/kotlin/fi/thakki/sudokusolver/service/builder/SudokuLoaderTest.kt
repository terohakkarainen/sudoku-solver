package fi.thakki.sudokusolver.service.builder

import assertk.assertThat
import assertk.assertions.hasMessage
import fi.thakki.sudokusolver.application.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Dimension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SudokuLoaderTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `Dimension with no standard layout does not have a builder`() {
        val sudokuFile = SudokuFile().apply {
            dimension = Dimension(5)
            symbols = setOf('f', 'o', 'b', 'a', 'r')
            givens = listOf("foo", "bar")
        }

        assertThat(
            assertThrows<IllegalArgumentException> {
                SudokuLoader.builderFor(sudokuFile, messageBroker)
            }
        ).hasMessage("No standard layout for dimension 5")
    }
}
