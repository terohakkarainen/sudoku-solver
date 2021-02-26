package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CoordinatesTest {

    @Test
    fun `coordinate creation failure cases`() {
        listOf(
            Pair(-1, 0),
            Pair(0, -1),
            Pair(-1, -1),
            Pair(Int.MIN_VALUE, Int.MIN_VALUE)
        ).forEach { input ->
            assertThrows<IllegalArgumentException> {
                Coordinates(input.first, input.second)
            }
        }
    }

    @Test
    fun `coordinate creation happy cases`() {
        listOf(
            Pair(0, 0),
            Pair(1, 0),
            Pair(0, 1),
            Pair(Int.MAX_VALUE, Int.MAX_VALUE)
        ).forEach { input ->
            val result = Coordinates(input.first, input.second)
            assertThat(result.x).isEqualTo(input.first)
            assertThat(result.y).isEqualTo(input.second)
        }
    }
}
