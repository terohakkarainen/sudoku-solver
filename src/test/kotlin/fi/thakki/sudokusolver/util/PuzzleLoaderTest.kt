package fi.thakki.sudokusolver.util

import assertk.assertThat
import assertk.assertions.hasMessage
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Dimension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleLoaderTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `Dimension with no standard layout does not have a builder`() {
        val puzzleFile = PuzzleFile().apply {
            dimension = Dimension(5)
            symbols = setOf('f', 'o', 'b', 'a', 'r')
            givens = listOf("foo", "bar")
        }

        assertThat(
            assertThrows<IllegalArgumentException> {
                PuzzleLoader.builderFor(puzzleFile, messageBroker)
            }
        ).hasMessage("No standard layout for dimension 5")
    }
}
