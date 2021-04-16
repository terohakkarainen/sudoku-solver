package fi.thakki.sudokusolver

import assertk.assertThat
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.application.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.service.solver.GuessingSolver
import fi.thakki.sudokusolver.service.builder.SudokuLoader
import org.junit.jupiter.api.Test

class SudokuSolvingTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `sample sudokus can be solved with analyzer`() {
        sampleSudokuFilenames.forEach { sudokuFileName ->
            val sudoku = this::class.java.classLoader.getResourceAsStream(sudokuFileName).use { inputStream ->
                SudokuLoader.newSudokuFromStream(checkNotNull(inputStream), messageBroker)
            }
            SudokuAnalyzer(sudoku, messageBroker).analyze(Int.MAX_VALUE)
            assertThat(sudoku.state).isEqualTo(Sudoku.State.COMPLETE)
        }
    }

    @Test
    fun `sample sudokus can be solved by guessing`() {
        sampleSudokuFilenames.forEach { sudokuFileName ->
            val sudoku = this::class.java.classLoader.getResourceAsStream(sudokuFileName).use { inputStream ->
                SudokuLoader.newSudokuFromStream(checkNotNull(inputStream), messageBroker)
            }
            val solvedSudoku = checkNotNull(GuessingSolver(sudoku, messageBroker).solve())
            assertThat(solvedSudoku.state).isEqualTo(Sudoku.State.COMPLETE)
        }
    }

    companion object {
        val sampleSudokuFilenames = listOf(
            "expert.yml",
            "expert2.yml",
            "expert3.yml",
            "expert4.yml",
            "sudoku.yml",
            "sudoku2.yml",
            "small.yml",
            "7x7.yml"
        )
    }
}
