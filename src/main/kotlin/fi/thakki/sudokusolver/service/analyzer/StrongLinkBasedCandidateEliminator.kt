package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.service.SudokuMutationService
import fi.thakki.sudokusolver.util.SudokuTraverser

class StrongLinkBasedCandidateEliminator(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private val sudokuTraverser = SudokuTraverser(sudoku)

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(
            this::eliminateCandidatesUsingStrongLinksInRegions,
            this::eliminateCandidatesInRegionsWithStrongLinksInBands,
            this::eliminateCandidatesInRegionsWithStrongLinksInStacks,
            this::eliminateOtherCandidatesInBiChoiceCellPair
        )

    private fun eliminateCandidatesUsingStrongLinksInRegions(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            sudoku.regions.flatMap { region ->
                region.analysis.strongLinks.map { strongLink ->
                    when {
                        strongLink.firstCell.coordinates.x == strongLink.secondCell.coordinates.x ->
                            eliminateCandidateFromCellsExcluding(
                                strongLink.symbol,
                                SudokuTraverser(sudoku).stackOf(strongLink.firstCell),
                                region,
                                "Strong link $strongLink in region also affects stack"
                            )
                        strongLink.firstCell.coordinates.y == strongLink.secondCell.coordinates.y ->
                            eliminateCandidateFromCellsExcluding(
                                strongLink.symbol,
                                SudokuTraverser(sudoku).bandOf(strongLink.firstCell),
                                region,
                                "Strong link $strongLink in region also affects band"
                            )
                        else -> AnalyzeResult.NoChanges
                    }
                }
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInBands(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            sudoku.bands.flatMap { band ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(band, "band")
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInStacks(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            sudoku.stacks.flatMap { stack ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(stack, "stack")
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(
        cellCollection: CellCollection,
        collectionName: String
    ): List<AnalyzeResult> =
        cellCollection.analysis.strongLinks.map { strongLink ->
            when {
                sudokuTraverser.regionOf(strongLink.firstCell) ==
                        sudokuTraverser.regionOf(strongLink.secondCell) ->
                    eliminateCandidateFromCellsExcluding(
                        strongLink.symbol,
                        SudokuTraverser(sudoku).regionOf(strongLink.firstCell),
                        cellCollection.cells,
                        "Strong link $strongLink in $collectionName also in region"
                    )
                else -> AnalyzeResult.NoChanges
            }
        }

    private fun eliminateCandidateFromCellsExcluding(
        candidate: Symbol,
        cells: Collection<Cell>,
        excluding: Collection<Cell>,
        messagePrefix: String
    ): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            cells.minus(excluding).map { cell ->
                if (cell.type == CellValueType.SETTABLE && cell.analysis.candidates.contains(candidate)) {
                    SudokuMutationService(sudoku).removeCandidate(cell.coordinates, candidate) { message ->
                        messageBroker.message("$messagePrefix: $message")
                    }
                    AnalyzeResult.CandidatesEliminated
                } else AnalyzeResult.NoChanges
            }
        )

    private fun eliminateOtherCandidatesInBiChoiceCellPair(): AnalyzeResult {
        val biChoiceCellPairs =
            sudoku.allCellCollections()
                .map { it.analysis.strongLinks }
                .flatten()
                .map { link ->
                    Pair(link.firstCell.coordinates, link.secondCell.coordinates) to link.symbol
                }.groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                ).mapValues { mapEntry ->
                    mapEntry.value.toSet()
                }.filter { mapEntry ->
                    mapEntry.value.size == 2
                }
        return if (biChoiceCellPairs.isNotEmpty()) {
            AnalyzeResult.combinedResultOf(
                biChoiceCellPairs.flatMap { mapEntry ->
                    listOf(mapEntry.key.first, mapEntry.key.second).map { coordinates ->
                        if (sudokuTraverser.cellAt(coordinates).analysis.candidates.size > 2) {
                            SudokuMutationService(sudoku).setCellCandidates(
                                coordinates,
                                mapEntry.value
                            ) { message ->
                                messageBroker.message("Bi-choice cell candidates eliminated: $message")
                            }
                            AnalyzeResult.CandidatesEliminated
                        } else AnalyzeResult.NoChanges
                    }
                }
            )
        } else AnalyzeResult.NoChanges
    }
}
