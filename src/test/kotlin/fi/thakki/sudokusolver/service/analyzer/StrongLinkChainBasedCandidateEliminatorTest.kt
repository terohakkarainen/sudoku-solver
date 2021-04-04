package fi.thakki.sudokusolver.service.analyzer

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkChain
import fi.thakki.sudokusolver.util.PuzzleTraverser
import fi.thakki.sudokusolver.util.StandardPuzzleBuilder
import org.junit.jupiter.api.Test

internal class StrongLinkChainBasedCandidateEliminatorTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `candidates are eliminated from chain end intersections`() {
        val symbol = '1'
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val puzzleTraverser = PuzzleTraverser(puzzle)

        puzzle.analysis.strongLinkChains = setOf(
            StrongLinkChain(
                symbol,
                listOf(
                    StrongLink(
                        symbol,
                        puzzleTraverser.cellAt(0, 8),
                        puzzleTraverser.cellAt(3, 8)
                    ),
                    StrongLink(
                        symbol,
                        puzzleTraverser.cellAt(3, 8),
                        puzzleTraverser.cellAt(5, 6)
                    ),
                    StrongLink(
                        symbol,
                        puzzleTraverser.cellAt(5, 6),
                        puzzleTraverser.cellAt(5, 0)
                    ),
                )
            )
        )

        val intersectionCoords = listOf(
            Coordinates(0, 0),
            Coordinates(5, 8)
        )

        intersectionCoords.forEach { coords ->
            assertThat(puzzleTraverser.cellAt(coords).analysis.candidates).contains(symbol)
        }

        val result = StrongLinkChainBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        intersectionCoords.forEach { coords ->
            assertThat(puzzleTraverser.cellAt(coords).analysis.candidates).doesNotContain(symbol)
        }
    }
}
