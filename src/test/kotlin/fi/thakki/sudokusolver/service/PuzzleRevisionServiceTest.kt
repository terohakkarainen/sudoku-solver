package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.util.StandardPuzzleBuilder
import fi.thakki.sudokusolver.util.PuzzleTraverser
import org.junit.jupiter.api.Test

internal class PuzzleRevisionServiceTest {

    private val messageBroker = ConsoleApplicationMessageBroker
    private val someCoordinates = Coordinates(0, 0)
    private val someValue = '5'

    @Test
    fun `Puzzle revision can be stored and retrieved`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val initialRevisionDescription = PuzzleRevisionService.newRevision(puzzle)

        PuzzleTraverser(puzzle).cellAt(someCoordinates).value = someValue

        PuzzleRevisionService.newRevision(puzzle)

        val restoredRevision = PuzzleRevisionService.previousRevision()

        assertThat(restoredRevision.description).isEqualTo(initialRevisionDescription)
        assertThat(PuzzleTraverser(restoredRevision.puzzle).cellAt(someCoordinates).value).isNull()
    }
}
