package fi.thakki.sudokusolver

import assertk.assertThat
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.util.SudokuLoader
import org.junit.jupiter.api.Test

class SudokuSolvingTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `sample sudokus can be solved`() {
        listOf(
            "expert.yml",
            "expert2.yml",
            "expert3.yml",
            "expert4.yml",
            "sudoku.yml",
            "sudoku2.yml",
            "small.yml",
            "7x7.yml"
        ).forEach { sudokuFilename ->
            val sudoku = this::class.java.classLoader.getResourceAsStream(sudokuFilename).use { inputStream ->
                SudokuLoader.newSudokuFromStream(checkNotNull(inputStream), messageBroker)
            }
            SudokuAnalyzer(sudoku, messageBroker).analyze(Int.MAX_VALUE)
            assertThat(sudoku.state).isEqualTo(Sudoku.State.COMPLETE)
        }
    }
}
