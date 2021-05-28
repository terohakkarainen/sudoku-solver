package fi.thakki.sudokusolver.engine.service.analyzer

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import org.junit.jupiter.api.Test

internal class CandidateBasedCandidateEliminatorTest {

    private val messageBroker = DiscardingMessageBroker

    @Test
    fun `eliminateBandOrStackCandidatesOnlyInRegion() - happy case`() {
        val sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val candidateToEliminate = '9'
        val region = sudoku.regions.first()
        val firstBand = sudoku.bands[0]
        val regionCellsInFirstBand = region.cells.intersect(firstBand)

        region.cells.forEach { it.analysis.candidates -= candidateToEliminate }
        regionCellsInFirstBand.forEach { it.analysis.candidates += candidateToEliminate }

        val result =
            CandidateBasedCandidateEliminator(sudoku, messageBroker).eliminateBandOrStackCandidatesOnlyInRegion()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        firstBand.cells.subtract(region.cells).let { firstBandCellsNotInRegion ->
            firstBandCellsNotInRegion.forEach { cell ->
                assertThat(cell.analysis.candidates).doesNotContain(candidateToEliminate)
            }
        }
    }

    @Test
    fun `eliminateRegionCandidatesOnlyInBandOrStack() - happy case`() {
        val sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_9X9, messageBroker).build()
        val candidateToEliminate = '1'
        val region = sudoku.regions.first()
        val firstBand = sudoku.bands[0]
        val regionCellsInFirstBand = region.cells.intersect(firstBand)

        firstBand.cells.forEach { it.analysis.candidates -= candidateToEliminate }
        regionCellsInFirstBand.forEach { it.analysis.candidates += candidateToEliminate }

        val result =
            CandidateBasedCandidateEliminator(sudoku, messageBroker).eliminateRegionCandidatesOnlyInBandOrStack()

        assertThat(result).isEqualTo(AnalyzeResult.CandidatesEliminated)
        region.cells.subtract(firstBand.cells).let { regionCellsNotInFirstBand ->
            regionCellsNotInFirstBand.forEach { cell ->
                assertThat(cell.analysis.candidates).doesNotContain(candidateToEliminate)
            }
        }
    }
}
