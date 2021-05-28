package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.CellCollection
import fi.thakki.sudokusolver.engine.model.StrongLink
import fi.thakki.sudokusolver.engine.model.StrongLinkChain
import fi.thakki.sudokusolver.engine.model.StrongLinkType
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService

class StrongLinkUpdater(private val sudoku: Sudoku) {

    fun updateStrongLinks(): AnalyzeResult {
        resetAllStrongLinks()

        // Find new strong links.
        sudoku.bands.forEach { band -> findStrongLinksInCollection(band, StrongLinkType.BAND) }
        sudoku.stacks.forEach { stack -> findStrongLinksInCollection(stack, StrongLinkType.STACK) }
        sudoku.regions.forEach { region -> findStrongLinksInCollection(region, StrongLinkType.REGION) }

        // Find chains.
        sudoku.allCellCollections().map { it.analysis.strongLinks }
            .reduce { acc, strongLinkSet -> acc.union(strongLinkSet) }
            .let { allStrongLinks ->
                findStrongLinkChains(allStrongLinks.toSet())
            }

        // Strong link update does not imply new analyze round.
        return AnalyzeResult.NoChanges
    }

    private fun resetAllStrongLinks() {
        sudoku.cells.cellsWithoutValue().forEach { cell -> cell.analysis.strongLinks = emptySet() }
        listOf(sudoku.bands, sudoku.stacks, sudoku.regions).forEach { cellCollection ->
            cellCollection.forEach { it.analysis.strongLinks = emptySet() }
        }
        sudoku.analysis.strongLinkChains = emptySet()
    }

    private fun findStrongLinksInCollection(cellCollection: CellCollection, strongLinkType: StrongLinkType) {
        sudoku.symbols.forEach { symbol ->
            cellCollection.cellsWithoutValue()
                .filter { it.analysis.candidates.contains(symbol) }
                .let { cellsWithSymbolCandidate ->
                    if (cellsWithSymbolCandidate.size == 2) {
                        SudokuMutationService(sudoku).addStrongLink(
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
            sudoku.analysis.strongLinkChains = linkChains
        }
    }

    private fun findStrongLinkChainsForSymbol(symbol: Symbol, strongLinks: Set<StrongLink>): Set<StrongLinkChain> =
        strongLinks.flatMap { strongLink ->
            strongLinks.minus(strongLink).let { otherStrongLinks ->
                listOf(
                    findChainStartingWith(strongLink, otherStrongLinks),
                    findChainStartingWith(strongLink.reverse(), otherStrongLinks)
                ).mapNotNull { chainStrongLinks ->
                    if (StrongLinkChain.isAcceptableChainLength(chainStrongLinks.size) &&
                        StrongLinkChain.areNotCircular(chainStrongLinks)
                    ) {
                        StrongLinkChain(symbol, chainStrongLinks)
                    } else null
                }
            }
        }.toSet()

    private fun findChainStartingWith(strongLink: StrongLink, otherStrongLinks: Set<StrongLink>): List<StrongLink> =
        otherStrongLinks.toList().let { others ->
            // Try to find next link from other strong links in both directions.
            others.plus(others.map { it.reverse() }).find { other ->
                other.firstCell == strongLink.secondCell
            }?.let { nextLink ->
                listOf(strongLink).plus(
                    findChainStartingWith(nextLink, otherStrongLinks.minus(nextLink))
                )
            } ?: listOf(strongLink)
        }
}
