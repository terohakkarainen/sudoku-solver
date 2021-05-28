package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.CellCollection
import fi.thakki.sudokusolver.engine.model.Region
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService

class CandidateBasedCandidateEliminator(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private val sudokuTraverser = SudokuTraverser(sudoku)

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(
            this::eliminateBandOrStackCandidatesOnlyInRegion,
            this::eliminateRegionCandidatesOnlyInBandOrStack
        )

    internal fun eliminateBandOrStackCandidatesOnlyInRegion(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            sudoku.symbols.flatMap { symbol ->
                sudoku.regions.map { region ->
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
            sudoku.symbols.flatMap { symbol ->
                sudoku.bands.map { band ->
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
        val haveCommonStack = sudokuTraverser.inSameStack(*cells.toTypedArray())
        if (haveCommonStack) {
            return sudoku.stacks[cells.first().coordinates.x]
        }
        val haveCommonBand = sudokuTraverser.inSameBand(*cells.toTypedArray())
        if (haveCommonBand) {
            return sudoku.bands[cells.first().coordinates.y]
        }
        return null
    }

    private fun commonRegion(cells: Collection<Cell>): Region? =
        if (sudokuTraverser.inSameRegion(*cells.toTypedArray())) {
            sudokuTraverser.regionOf(cells.first())
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
                    SudokuMutationService(sudoku).removeCandidate(
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
