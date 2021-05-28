package fi.thakki.sudokusolver.engine.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import fi.thakki.sudokusolver.engine.service.builder.StandardSymbols
import org.junit.jupiter.api.Test

internal class StrongLinkTest {

    private val someSymbol = 'a'
    private val otherSymbol = 'b'
    private val someCell = Cell(Coordinates(0, 0), StandardSymbols.SYMBOLS_19)
    private val otherCell = Cell(Coordinates(1, 1), StandardSymbols.SYMBOLS_19)

    @Test
    fun `strong links are equal if the cells are the same, no matter the direction`() {
        val link1 = StrongLink(someSymbol, someCell, otherCell)
        val link2 = StrongLink(someSymbol, otherCell, someCell)

        assertThat(link1).isEqualTo(link2)
    }

    @Test
    fun `strong links are equal only if the symbol is the same`() {
        val link1 = StrongLink(someSymbol, someCell, otherCell)
        val link2 = StrongLink(otherSymbol, someCell, otherCell)

        assertThat(link1).isNotEqualTo(link2)
    }

    @Test
    fun `strong link can be reversed`() {
        val link = StrongLink(someSymbol, someCell, otherCell)
        val reverseLink = link.reverse()

        assertThat(link.firstCell).isEqualTo(reverseLink.secondCell)
        assertThat(link.secondCell).isEqualTo(reverseLink.firstCell)
    }
}
