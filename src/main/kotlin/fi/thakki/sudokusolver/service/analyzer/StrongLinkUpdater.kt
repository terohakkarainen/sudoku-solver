package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.extensions.unsetCells
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.service.PuzzleMutationService

class StrongLinkUpdater(private val puzzle: Puzzle) {

    fun updateStrongLinks(): AnalyzeResult {
        // Reset existing strong links.
        puzzle.cells.unsetCells().forEach { cell -> cell.analysis.strongLinks = emptySet() }
        listOf(puzzle.bands, puzzle.stacks, puzzle.regions).forEach { cellCollection ->
            cellCollection.forEach { it.analysis.strongLinks = emptySet() }
        }
        puzzle.bands.forEach { band -> updateStrongLinksForCells(band, StrongLink.LinkType.BAND) }
        puzzle.stacks.forEach { stack -> updateStrongLinksForCells(stack, StrongLink.LinkType.STACK) }
        puzzle.regions.forEach { region -> updateStrongLinksForCells(region, StrongLink.LinkType.REGION) }
        return AnalyzeResult.NoChanges // Strong link update does not imply new analyze round.
    }

    private fun updateStrongLinksForCells(cellCollection: CellCollection, strongLinkType: StrongLink.LinkType) {
        puzzle.symbols.forEach { symbol ->
            cellCollection.cells.unsetCells()
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
