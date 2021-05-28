package fi.thakki.sudokusolver.engine.util

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PermutationsTest {

    @Test
    fun `correct permutations are found - tuple size 2`() {
        val result = permutations(setOf(1, 2, 3), 2)

        assertThat(result).isEqualTo(
            setOf(
                setOf(1, 2),
                setOf(1, 3),
                setOf(2, 3)
            )
        )
    }

    @Test
    fun `correct permutations are found - tuple size 1`() {
        val result = permutations(setOf(1, 2, 3), 1)

        assertThat(result).isEqualTo(
            setOf(
                setOf(1),
                setOf(2),
                setOf(3)
            )
        )
    }

    @Test
    fun `tuple size of 0 returns empty results`() {
        val result = permutations(setOf(1, 2, 3), 0)

        assertThat(result).isEmpty()
    }

    @Test
    fun `tuple size greater than number of elements returns empty results`() {
        val result = permutations(setOf(1, 2, 3), 4)

        assertThat(result).isEmpty()
    }

    @Test
    fun `empty element set returns empty results`() {
        val result = permutations(emptySet(), 2)

        assertThat(result).isEmpty()
    }

    @Test
    fun `negative tuple size is handled`() {
        assertThat(
            assertThrows<IllegalArgumentException> {
                permutations(emptySet(), -1)
            }
        ).hasMessage("Tuple size must be positive integer")
    }
}
