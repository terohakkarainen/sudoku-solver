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
        val initialRevisionDescription = SudokuRevisionService.newRevision(sudoku)

        SudokuTraverser(sudoku).cellAt(someCoordinates).value = someValue

        SudokuRevisionService.newRevision(sudoku)

        val restoredRevision = SudokuRevisionService.previousRevision()

        assertThat(restoredRevision.description).isEqualTo(initialRevisionDescription)
        assertThat(SudokuTraverser(restoredRevision.sudoku).cellAt(someCoordinates).value).isNull()
    }
}
