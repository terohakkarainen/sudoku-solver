package fi.thakki.sudokusolver.engine.service.analyzer

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.Coordinate
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.StrongLink
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.builder.StandardSymbols
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import org.junit.jupiter.api.Test

internal class StrongLinkUpdaterTest {

    private val messageBroker = DiscardingMessageBroker
    private val sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
    private val someSymbol = 'a'
    private val otherSymbol = 'b'

    @Test
    fun `chain is found among links`() {
        val link1 = StrongLink(someSymbol, newCell(6, 6), newCell(7, 7))
        val link2 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link3 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link4 = StrongLink(someSymbol, newCell(1, 1), newCell(1, 0))
        val link5 = StrongLink(someSymbol, newCell(5, 5), newCell(6, 6))

        StrongLinkUpdater(sudoku).findStrongLinkChains(setOf(link1, link2, link3, link4, link5))

        with(sudoku.analysis.strongLinkChains.single()) {
            assertThat(symbol).isEqualTo(someSymbol)
            assertThat(strongLinks).isEqualTo(listOf(link2, link3, link4))
        }
    }

    @Test
    fun `chain is found even though link is in wrong direction`() {
        val link1 = StrongLink(someSymbol, newCell(6, 6), newCell(7, 7))
        val link2 = StrongLink(someSymbol, newCell(7, 7), newCell(5, 5))
        val link3 = StrongLink(someSymbol, newCell(4, 4), newCell(5, 5))

        StrongLinkUpdater(sudoku).findStrongLinkChains(setOf(link1, link2, link3))

        with(sudoku.analysis.strongLinkChains.single()) {
            assertThat(symbol).isEqualTo(someSymbol)
        }
    }

    @Test
    fun `links for other symbols are not used in chains`() {
        val link1 = StrongLink(someSymbol, newCell(6, 6), newCell(7, 7))
        val link2 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link3 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link4 = StrongLink(otherSymbol, newCell(1, 1), newCell(1, 0))
        val link5 = StrongLink(someSymbol, newCell(5, 5), newCell(6, 6))

        StrongLinkUpdater(sudoku).findStrongLinkChains(setOf(link1, link2, link3, link4, link5))

        assertThat(sudoku.analysis.strongLinkChains).isEmpty()
    }

    @Test
    fun `chain of two links is not enough`() {
        val link1 = StrongLink(someSymbol, newCell(6, 6), newCell(7, 7))
        val link2 = StrongLink(someSymbol, newCell(0, 0), newCell(0, 1))
        val link3 = StrongLink(someSymbol, newCell(0, 1), newCell(1, 1))
        val link4 = StrongLink(someSymbol, newCell(5, 5), newCell(6, 6))

        StrongLinkUpdater(sudoku).findStrongLinkChains(setOf(link1, link2, link3, link4))

        assertThat(sudoku.analysis.strongLinkChains).isEmpty()
    }

    @Test
    fun `circular chain is not accepted`() {
        val link1 = StrongLink(someSymbol, newCell(3, 3), newCell(4, 3))
        val link2 = StrongLink(someSymbol, newCell(4, 3), newCell(4, 1))
        val link3 = StrongLink(someSymbol, newCell(4, 1), newCell(3, 1))
        val link4 = StrongLink(someSymbol, newCell(3, 1), newCell(3, 3))
        val link5 = StrongLink(someSymbol, newCell(3, 3), newCell(1, 1))

        StrongLinkUpdater(sudoku).findStrongLinkChains(setOf(link1, link2, link3, link4, link5))

        with(sudoku.analysis.strongLinkChains.single()) {
            assertThat(symbol).isEqualTo(someSymbol)
            assertThat(strongLinks).isEqualTo(listOf(link2.reverse(), link1.reverse(), link5))
        }
    }

    private fun newCell(x: Coordinate, y: Coordinate) =
        Cell(Coordinates(x, y), StandardSymbols.SYMBOLS_19)
}
