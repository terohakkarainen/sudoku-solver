package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class CandidateBasedCandidateEliminator(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun eliminateCandidates(): AnalyzeResult =
        when {
            eliminateBandOrStackCandidatesOnlyInRegion() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            eliminateRegionCandidatesOnlyInBandOrStack() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            else -> AnalyzeResult.NoChanges
        }

    internal fun eliminateBandOrStackCandidatesOnlyInRegion(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.symbols.flatMap { symbol ->
                puzzle.regions.map { region ->
                    region.cellsWithoutValue()
                        .filter { it.analysis.candidates.contains(symbol) }
                        .let { regionCellsWithSymbol ->
                            commonBandOrStack(regionCellsWithSymbol)?.let { commonBandOrStack ->
                                if (region.cells.intersect(commonBandOrStack.cells).size ==
                                    regionCellsWithSymbol.size
                                ) {
                                    removeCandidateFromCollectionExcluding(
                                        symbol,
                                        commonBandOrStack,
                                        regionCellsWithSymbol
                                    )
                                } else AnalyzeResult.NoChanges
                            } ?: AnalyzeResult.NoChanges
                        }
                }
            }
        )

    internal fun eliminateRegionCandidatesOnlyInBandOrStack(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.symbols.flatMap { symbol ->
                puzzle.bands.map { band ->
                    band.cellsWithoutValue()
                        .filter { it.analysis.candidates.contains(symbol) }
                        .let { collectionCellsWithSymbol ->
                            commonRegion(collectionCellsWithSymbol)?.let { commonRegion ->
                                removeCandidateFromCollectionExcluding(symbol, commonRegion, collectionCellsWithSymbol)
                            } ?: AnalyzeResult.NoChanges
                        }
                }
            }
        )

    private fun commonBandOrStack(cells: Collection<Cell>): CellCollection? {
        val haveCommonStack = puzzleTraverser.inSameStack(*cells.toTypedArray())
        if (haveCommonStack) {
            return puzzle.stacks[cells.first().coordinates.x]
        }
        val haveCommonBand = puzzleTraverser.inSameBand(*cells.toTypedArray())
        if (haveCommonBand) {
            return puzzle.bands[cells.first().coordinates.y]
        }
        return null
    }

    private fun commonRegion(cells: Collection<Cell>): Region? =
        if (puzzleTraverser.inSameRegion(*cells.toTypedArray())) {
            puzzleTraverser.regionOf(cells.first())
        } else null

    private fun removeCandidateFromCollectionExcluding(
        candidate: Symbol,
        commonCellCollection: CellCollection,
        excludedCells: Collection<Cell>
    ): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            commonCellCollection.cellsWithoutValue()
                .subtract(excludedCells)
                .filter { it.analysis.candidates.contains(candidate) }
                .map { affectedCell ->
                    PuzzleMutationService(puzzle).removeCandidate(
                        affectedCell.coordinates,
                        candidate
                    ) { message ->
                        messageBroker.message(
                            "Candidate eliminated by dominance in " +
                                    "${commonCellCollection::class.simpleName?.toLowerCase()}: $message"
                        )
                    }
                    AnalyzeResult.CandidatesEliminated
                }
        )
}
