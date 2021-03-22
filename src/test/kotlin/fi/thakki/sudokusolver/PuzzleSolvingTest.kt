package fi.thakki.sudokusolver

import assertk.assertThat
import assertk.assertions.isTrue
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer
import fi.thakki.sudokusolver.util.PuzzleLoader
import org.junit.jupiter.api.Test

class PuzzleSolvingTest {

    @Test
    fun `sample puzzles can be solved`() {
        listOf(
            "expert.yml",
            "expert2.yml",
            "expert3.yml",
            "expert4.yml",
            "puzzle.yml",
            "small.yml"
        ).forEach { puzzleFile ->
            val puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFile)
            PuzzleAnalyzer(puzzle).analyze(Int.MAX_VALUE)
            assertThat(puzzle.isComplete()).isTrue()
        }
    }
}
