package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DimensionTest {

    @Test
    fun `dimension creation failure cases`() {
        listOf(
            0,
            -1,
            Int.MIN_VALUE
        ).forEach { input ->
            assertThrows<IllegalArgumentException> {
                Dimension(input)
            }
        }
    }

    @Test
    fun `dimension creation happy cases`() {
        listOf(
            1,
            Int.MAX_VALUE
        ).forEach { input ->
            assertThat(Dimension(input).value).isEqualTo(input)
        }
    }
}
