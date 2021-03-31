package fi.thakki.sudokusolver

import assertk.assertThat
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer
import fi.thakki.sudokusolver.util.PuzzleLoader
import org.junit.jupiter.api.Test

class PuzzleSolvingTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `sample puzzles can be solved`() {
        listOf(
            "expert.yml",
            "expert2.yml",
            "expert3.yml",
            "expert4.yml",
            "puzzle.yml",
            "puzzle2.yml",
            "small.yml",
            "7x7.yml"
        ).forEach { puzzleFile ->
            val puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFile, messageBroker)
            PuzzleAnalyzer(puzzle, messageBroker).analyze(Int.MAX_VALUE)
            assertThat(puzzle.state).isEqualTo(Puzzle.State.COMPLETE)
        }
    }
}
