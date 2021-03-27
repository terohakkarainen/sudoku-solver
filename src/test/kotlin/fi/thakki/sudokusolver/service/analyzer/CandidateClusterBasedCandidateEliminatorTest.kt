package fi.thakki.sudokusolver.service.analyzer

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.util.StandardPuzzleBuilder
import org.junit.jupiter.api.Test

internal class CandidateClusterBasedCandidateEliminatorTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `finds cluster-3 in large candidate group`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val firstBand = puzzle.bands[0]
        val clusterCandidates = setOf('1', '2', '3')

        // Only cells[0..2] have candidates 1-3.
        for (index in 3..8) {
            firstBand[index].analysis.candidates -= clusterCandidates.plus('4')
        }
        // Cluster cells contain only part of the triplet, but also other candidates.
        firstBand[0].analysis.candidates -= setOf('2', '4')
        firstBand[1].analysis.candidates -= setOf('1', '4')
        firstBand[2].analysis.candidates -= setOf('3', '4')

        // Candidate '4' only in three last cells.
        for (index in 6..8) {
            firstBand[index].analysis.candidates += '4'
        }

        val result = CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        assertThat(firstBand[0].analysis.candidates).isEqualTo(clusterCandidates.minus('2'))
        assertThat(firstBand[1].analysis.candidates).isEqualTo(clusterCandidates.minus('1'))
        assertThat(firstBand[2].analysis.candidates).isEqualTo(clusterCandidates.minus('3'))

        for (index in 3..8) {
            clusterCandidates.forEach { candidate ->
                assertThat(firstBand[index].analysis.candidates).doesNotContain(candidate)
            }
        }
    }

    @Test
    fun `finds cluster-3 in small candidate group`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val firstBand = puzzle.bands[0]
        val clusterCandidates = setOf('1', '2', '3')

        // Only cells[0..2] have candidates 1-3.
        for (index in 3..8) {
            firstBand[index].analysis.candidates -= clusterCandidates
        }
        // Cluster cells contain only part of the triplet and no other candidates.
        firstBand[0].analysis.candidates = clusterCandidates.minus('2')
        firstBand[1].analysis.candidates = clusterCandidates.minus('1')
        firstBand[2].analysis.candidates = clusterCandidates.minus('3')

        val result = CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()

        assertThat(result).isEqualTo(AnalyzeResult.NoChanges)
    }

    @Test
    fun `no clusters to be found, no changes`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val result = CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()
        assertThat(result).isEqualTo(AnalyzeResult.NoChanges)
    }

    @Test
    fun `cluster sizes for dimension-9 puzzle`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val result = CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).clusterSizesForPuzzle()
        assertThat(result).isEqualTo(setOf(3, 4, 5))
    }

    @Test
    fun `cluster sizes for dimension-4 puzzle`() {
        val puzzle = StandardPuzzleBuilder(StandardPuzzleBuilder.StandardLayout.STANDARD_4X4, messageBroker).build()
        val result = CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).clusterSizesForPuzzle()
        assertThat(result).isEqualTo(emptySet())
    }
}
