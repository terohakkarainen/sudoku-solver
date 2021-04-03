package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkChain
import fi.thakki.sudokusolver.model.StrongLinkChain.Companion.MINIMUM_CHAIN_LENGTH
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkUpdater(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun updateStrongLinks(): AnalyzeResult {
        resetAllStrongLinks()

        // Find new strong links.
        puzzle.bands.forEach { band -> findStrongLinksInCollection(band, StrongLinkType.BAND) }
        puzzle.stacks.forEach { stack -> findStrongLinksInCollection(stack, StrongLinkType.STACK) }
        puzzle.regions.forEach { region -> findStrongLinksInCollection(region, StrongLinkType.REGION) }

        // Find chains.
        puzzle.allCellCollections().map { it.analysis.strongLinks }
            .reduce { acc, strongLinkSet -> acc.union(strongLinkSet) }
            .let { allStrongLinks ->
                findStrongLinkChains(allStrongLinks.toSet())
            }

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

    internal fun findStrongLinkChains(strongLinks: Set<StrongLink>) {
        val strongLinksBySymbol = strongLinks.groupBy { it.symbol }

        strongLinksBySymbol.keys.flatMap { symbol ->
            findStrongLinkChainsForSymbol(
                symbol,
                checkNotNull(strongLinksBySymbol[symbol]).toSet()
            )
        }.toSet().let { linkChains ->
            puzzle.analysis.strongLinkChains = linkChains
        }
    }

    private fun findStrongLinkChainsForSymbol(symbol: Symbol, strongLinks: Set<StrongLink>): Set<StrongLinkChain> =
        strongLinks.flatMap { strongLink ->
            strongLinks.minus(strongLink).let { otherStrongLinks ->
                listOf(
                    findChainStartingWith(strongLink, otherStrongLinks),
                    findChainStartingWith(strongLink.reverse(), otherStrongLinks)
                ).mapNotNull { strongLinkList ->
                    if (strongLinkList.size >= MINIMUM_CHAIN_LENGTH) {
                        StrongLinkChain(symbol, strongLinkList)
                    } else null
                }
            }
        }.toSet()

    private fun findChainStartingWith(strongLink: StrongLink, otherStrongLinks: Set<StrongLink>): List<StrongLink> =
        otherStrongLinks.find { other -> other.firstCell == strongLink.secondCell }?.let { nextLink ->
            listOf(strongLink).plus(
                findChainStartingWith(nextLink, otherStrongLinks.minus(nextLink))
            )
        } ?: listOf(strongLink)
}
