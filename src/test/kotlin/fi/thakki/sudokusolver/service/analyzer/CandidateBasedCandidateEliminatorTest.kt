package fi.thakki.sudokusolver.service.analyzer

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.util.PuzzleBuilder
import org.junit.jupiter.api.Test

internal class CandidateBasedCandidateEliminatorTest {

    @Test
    fun `eliminateBandOrStackCandidatesOnlyInRegion() - happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_9X9).build()
        val candidateToEliminate = '9'
        val region = puzzle.regions.first()
        val firstBand = puzzle.bands[0]
        val regionCellsInFirstBand = region.cells.intersect(firstBand)

        region.cells.forEach { it.analysis.candidates -= candidateToEliminate }
        regionCellsInFirstBand.forEach { it.analysis.candidates += candidateToEliminate }

        val result = CandidateBasedCandidateEliminator(puzzle).eliminateBandOrStackCandidatesOnlyInRegion()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        firstBand.cells.subtract(region.cells).let { firstBandCellsNotInRegion ->
            firstBandCellsNotInRegion.forEach { cell ->
                assertThat(cell.analysis.candidates).doesNotContain(candidateToEliminate)
            }
        }
    }

    @Test
    fun `eliminateRegionCandidatesOnlyInBandOrStack() - happy case`() {
        val puzzle = PuzzleBuilder(PuzzleBuilder.Layout.STANDARD_9X9).build()
        val candidateToEliminate = '1'
        val region = puzzle.regions.first()
        val firstBand = puzzle.bands[0]
        val regionCellsInFirstBand = region.cells.intersect(firstBand)

        firstBand.cells.forEach { it.analysis.candidates -= candidateToEliminate }
        regionCellsInFirstBand.forEach { it.analysis.candidates += candidateToEliminate }

        val result = CandidateBasedCandidateEliminator(puzzle).eliminateRegionCandidatesOnlyInBandOrStack()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        region.cells.subtract(firstBand.cells).let { regionCellsNotInFirstBand ->
            regionCellsNotInFirstBand.forEach { cell ->
                assertThat(cell.analysis.candidates).doesNotContain(candidateToEliminate)
            }
        }
    }
}
