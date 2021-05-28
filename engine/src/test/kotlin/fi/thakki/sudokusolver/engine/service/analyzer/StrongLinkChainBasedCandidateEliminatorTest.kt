package fi.thakki.sudokusolver.engine.service.analyzer

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.StrongLink
import fi.thakki.sudokusolver.engine.model.StrongLinkChain
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import org.junit.jupiter.api.Test

internal class StrongLinkChainBasedCandidateEliminatorTest {

    private val messageBroker = DiscardingMessageBroker

    @Test
    fun `candidates are eliminated from chain end intersections`() {
        val symbol = '1'
        val sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val sudokuTraverser = SudokuTraverser(sudoku)

        sudoku.analysis.strongLinkChains = setOf(
            StrongLinkChain(
                symbol,
                listOf(
                    StrongLink(
                        symbol,
                        sudokuTraverser.cellAt(0, 8),
                        sudokuTraverser.cellAt(3, 8)
                    ),
                    StrongLink(
                        symbol,
                        sudokuTraverser.cellAt(3, 8),
                        sudokuTraverser.cellAt(5, 6)
                    ),
                    StrongLink(
                        symbol,
                        sudokuTraverser.cellAt(5, 6),
                        sudokuTraverser.cellAt(5, 0)
                    ),
                )
            )
        )

        val intersectionCoords = listOf(
            Coordinates(0, 0),
            Coordinates(5, 8)
        )

        intersectionCoords.forEach { coords ->
            assertThat(sudokuTraverser.cellAt(coords).analysis.candidates).contains(symbol)
        }

        val result = StrongLinkChainBasedCandidateEliminator(sudoku, messageBroker).eliminateCandidates()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        intersectionCoords.forEach { coords ->
            assertThat(sudokuTraverser.cellAt(coords).analysis.candidates).doesNotContain(symbol)
        }
    }
}
