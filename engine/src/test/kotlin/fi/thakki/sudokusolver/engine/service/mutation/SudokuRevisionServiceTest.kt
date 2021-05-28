package fi.thakki.sudokusolver.engine.service.mutation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import org.junit.jupiter.api.Test

internal class SudokuRevisionServiceTest {

    private val messageBroker = DiscardingMessageBroker
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
