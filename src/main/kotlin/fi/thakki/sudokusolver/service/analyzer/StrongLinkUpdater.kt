package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.service.PuzzleMutationService

class StrongLinkUpdater(private val puzzle: Puzzle) {

    fun updateStrongLinks(): AnalyzeResult {
        // Reset existing strong links.
        puzzle.cells.cellsWithoutValue().forEach { cell -> cell.analysis.strongLinks = emptySet() }
        listOf(puzzle.bands, puzzle.stacks, puzzle.regions).forEach { cellCollection ->
            cellCollection.forEach { it.analysis.strongLinks = emptySet() }
        }
        puzzle.bands.forEach { band -> updateStrongLinksForCells(band, StrongLinkType.BAND) }
        puzzle.stacks.forEach { stack -> updateStrongLinksForCells(stack, StrongLinkType.STACK) }
        puzzle.regions.forEach { region -> updateStrongLinksForCells(region, StrongLinkType.REGION) }
        return AnalyzeResult.NoChanges // Strong link update does not imply new analyze round.
    }

    private fun updateStrongLinksForCells(cellCollection: CellCollection, strongLinkType: StrongLinkType) {
        puzzle.symbols.forEach { symbol ->
            cellCollection.cellsWithoutValue()
                .filter { it.analysis.candidates.contains(symbol) }
                .let { cellsWithSymbolCandidate ->
                    if (cellsWithSymbolCandidate.size == 2) {
                        PuzzleMutationService(puzzle).addStrongLink(
                            cellCollection,
                            symbol,
                            cellsWithSymbolCandidate.first(),
                            cellsWithSymbolCandidate.last(),
                            strongLinkType
                        )
                    }
                }
        }
    }
}
