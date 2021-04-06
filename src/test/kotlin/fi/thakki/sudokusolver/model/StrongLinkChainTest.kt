package fi.thakki.sudokusolver.model

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.util.StandardSymbols
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StrongLinkChainTest {

    private val someSymbol = 'a'
    private val otherSymbol = 'b'

    @Test
    fun `chain with no links cannot be created`() {
        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(someSymbol, emptyList())
            }
        ).hasMessage("Strong link chain size must be odd and greater or equal to 3")
    }

    @Test
    fun `chain with too few links cannot be created`() {
        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(
                    someSymbol,
                    listOf(
                        StrongLink(someSymbol, newCell(0, 0), newCell(1, 1)),
                        StrongLink(someSymbol, newCell(1, 1), newCell(2, 2))
                    )
                )
            }
        ).hasMessage("Strong link chain size must be odd and greater or equal to 3")
    }

    @Test
    fun `one link with other symbol fails chain creation`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(1, 1))
        val link2 = StrongLink(otherSymbol, newCell(1, 1), newCell(2, 2))
        val link3 = StrongLink(someSymbol, newCell(2, 2), newCell(3, 3))

        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(someSymbol, listOf(link1, link2, link3))
            }
        ).hasMessage("All strong links in chain must share the same symbol")
    }

    @Test
    fun `chain with other symbol fails creation`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(1, 1))
        val link2 = StrongLink(someSymbol, newCell(1, 1), newCell(2, 2))
        val link3 = StrongLink(someSymbol, newCell(2, 2), newCell(3, 3))

        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(otherSymbol, listOf(link1, link2, link3))
            }
        ).hasMessage("All strong links in chain must share the same symbol")
    }

    @Test
    fun `chain is the same regardless of direction`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(1, 1))
        val link2 = StrongLink(someSymbol, newCell(1, 1), newCell(2, 2))
        val link3 = StrongLink(someSymbol, newCell(2, 2), newCell(3, 3))

        val chain1 = StrongLinkChain(someSymbol, listOf(link1, link2, link3))
        val chain2 = StrongLinkChain(someSymbol, chain1.map { link -> link.reverse() }.reversed())

        assertThat(chain1).isEqualTo(chain2)
    }

    @Test
    fun `chains are the same if same cells are involved`() {
        val link1 = StrongLink(someSymbol, newCell(3, 4), newCell(3, 8))
        val link2 = StrongLink(someSymbol, newCell(4, 3), newCell(4, 8))
        val link3 = StrongLink(someSymbol, newCell(3, 4), newCell(4, 3))
        val link4 = StrongLink(someSymbol, newCell(3, 8), newCell(4, 8))

        val chain1 = StrongLinkChain(someSymbol, listOf(link1.reverse(), link3, link2))
        val chain2 = StrongLinkChain(someSymbol, listOf(link3.reverse(), link1, link4))

        assertThat(chain1).isEqualTo(chain2)
    }

    @Test
    fun `chain cannot contain same link twice`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link2 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link3 = StrongLink(someSymbol, newCell(1, 1), newCell(0, 1))

        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(someSymbol, listOf(link1, link2, link3))
            }
        ).hasMessage("Strong link chain must not contain the same strong link twice")
    }

    @Test
    fun `chain cannot have the same cell twice - meaning a circular chain`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link2 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link3 = StrongLink(someSymbol, newCell(1, 1), newCell(0, 0))

        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(someSymbol, listOf(link1, link2, link3))
            }
        ).hasMessage("Strong link chain must not be circular")
    }

    @Test
    fun `discontinuous chain is detected`() {
        val link1 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link2 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link3 = StrongLink(someSymbol, newCell(2, 1), newCell(3, 3))

        assertThat(
            assertThrows<IllegalArgumentException> {
                StrongLinkChain(someSymbol, listOf(link1, link2, link3))
            }
        ).hasMessage("Strong link chain must be continuous")
    }

    private fun newCell(x: Coordinate, y: Coordinate) =
        Cell(Coordinates(x, y), StandardSymbols.SYMBOLS_19)
}
