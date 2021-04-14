package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.util.StandardSudokuBuilder
import fi.thakki.sudokusolver.util.SudokuTraverser
import org.junit.jupiter.api.Test

internal class SudokuRevisionServiceTest {

    private val messageBroker = ConsoleApplicationMessageBroker
    private val someCoordinates = Coordinates(0, 0)
    private val someValue = '5'

    @Test
    fun `Sudoku revision can be stored and retrieved`() {
        val sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val serviceUnderTest = SudokuRevisionService()
        val initialDescription = "initial"
        val nextDescription = "change"
        val initialRevisionInfo = serviceUnderTest.newRevision(sudoku, initialDescription)

        with(initialRevisionInfo) {
            assertThat(number).isEqualTo(SudokuRevisionService.INITIAL_REVISION)
            assertThat(description).isEqualTo(initialDescription)
        }

        SudokuTraverser(sudoku).cellAt(someCoordinates).value = someValue

        with(serviceUnderTest.newRevision(sudoku, nextDescription)) {
            assertThat(number).isEqualTo(SudokuRevisionService.INITIAL_REVISION + 1)
            assertThat(description).isEqualTo(nextDescription)
        }

        with(serviceUnderTest.restorePreviousRevision()) {
            assertThat(number).isEqualTo(initialRevisionInfo.number)
            assertThat(description).isEqualTo(initialRevisionInfo.description)
            assertThat(SudokuTraverser(this.sudoku).cellAt(someCoordinates).value).isNull()
        }
    }
}
