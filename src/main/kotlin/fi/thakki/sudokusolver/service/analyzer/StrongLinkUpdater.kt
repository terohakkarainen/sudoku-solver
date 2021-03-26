package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkChain
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkUpdater(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun updateStrongLinks(): AnalyzeResult {
        resetAllStrongLinks()

        // Find new strong links and chains.
        puzzle.bands.forEach { band -> findStrongLinksInCollection(band, StrongLinkType.BAND) }
        puzzle.stacks.forEach { stack -> findStrongLinksInCollection(stack, StrongLinkType.STACK) }
        puzzle.regions.forEach { region -> findStrongLinksInCollection(region, StrongLinkType.REGION) }
        findStrongLinkChains(puzzle.allCellCollections().toSet())

        // Strong link update does not imply new analyze round.
        return AnalyzeResult.NoChanges
    }

    private fun resetAllStrongLinks() {
        puzzle.cells.cellsWithoutValue().forEach { cell -> cell.analysis.strongLinks = emptySet() }
        listOf(puzzle.bands, puzzle.stacks, puzzle.regions).forEach { cellCollection ->
            cellCollection.forEach { it.analysis.strongLinks = emptySet() }
        }
        puzzle.analysis.strongLinkChains = emptySet()
    }

    private fun findStrongLinksInCollection(cellCollection: CellCollection, strongLinkType: StrongLinkType) {
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

    private fun findStrongLinkChains(cellCollections: Set<CellCollection>) {
        val strongLinksBySymbol =
            cellCollections
                .map { it.analysis.strongLinks }
                .reduce { acc, strongLinkSet -> acc.union(strongLinkSet) }
                .groupBy { it.symbol }

        strongLinksBySymbol.keys.flatMap { symbol ->
            findStrongLinkChainsForSymbol(
                symbol,
                checkNotNull(strongLinksBySymbol[symbol])
            )
        }.toSet().let { linkChains ->
            puzzle.analysis.strongLinkChains = linkChains
        }
    }

    private fun findStrongLinkChainsForSymbol(symbol: Symbol, strongLinks: List<StrongLink>): Set<StrongLinkChain> {
        val result = mutableSetOf<StrongLinkChain>()

        strongLinks.forEach { startLink ->
            val otherStrongLinks = strongLinks.minus(startLink)
            val startLinkReversed = startLink.reverse()

            listOf(startLink, startLinkReversed).forEach { link ->
                findNextLinks(otherStrongLinks, link)
                    .forEach { nextLink ->
                        result.add(
                            StrongLinkChain(symbol, listOf(link, nextLink))
                        )
                    }
            }
        }

        return result
    }

    private fun findNextLinks(linksToSearch: List<StrongLink>, fromLink: StrongLink): Set<StrongLink> =
        linksToSearch.mapNotNull { nextLink ->
            if (nextLink.firstCell !in fromLink.cells() &&
                nextLink.secondCell !in fromLink.cells()
            ) {
                when {
                    puzzleTraverser.inSameCellCollection(fromLink.secondCell, nextLink.firstCell) -> nextLink
                    puzzleTraverser.inSameCellCollection(fromLink.secondCell, nextLink.secondCell) -> nextLink.reverse()
                    else -> null
                }
            } else null
        }.toSet()
}
