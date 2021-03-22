package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkBasedCandidateEliminator(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun eliminateCandidates(): AnalyzeResult =
        when {
            eliminateCandidatesUsingStrongLinksInRegions() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            eliminateCandidatesInRegionsWithStrongLinksInBands() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            eliminateCandidatesInRegionsWithStrongLinksInStacks() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            eliminateOtherCandidatesInBiChoiceCellPair() == AnalyzeResult.CandidatesEliminated ->
                AnalyzeResult.CandidatesEliminated
            else -> AnalyzeResult.NoChanges
        }

    private fun eliminateCandidatesUsingStrongLinksInRegions(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.regions.flatMap { region ->
                region.analysis.strongLinks.map { strongLink ->
                    when {
                        strongLink.firstCell.coordinates.x == strongLink.secondCell.coordinates.x ->
                            eliminateCandidateFromCellsExcluding(
                                strongLink.symbol,
                                PuzzleTraverser(puzzle).stackOf(strongLink.firstCell),
                                region,
                                "Strong link $strongLink in region also affects stack"
                            )
                        strongLink.firstCell.coordinates.y == strongLink.secondCell.coordinates.y ->
                            eliminateCandidateFromCellsExcluding(
                                strongLink.symbol,
                                PuzzleTraverser(puzzle).bandOf(strongLink.firstCell),
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
            puzzle.bands.flatMap { band ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(band, "band")
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInStacks(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.stacks.flatMap { stack ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(stack, "stack")
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(
        cellCollection: CellCollection,
        collectionName: String
    ): List<AnalyzeResult> =
        cellCollection.analysis.strongLinks.map { strongLink ->
            when {
                puzzleTraverser.regionOf(strongLink.firstCell) ==
                        puzzleTraverser.regionOf(strongLink.secondCell) ->
                    eliminateCandidateFromCellsExcluding(
                        strongLink.symbol,
                        PuzzleTraverser(puzzle).regionOf(strongLink.firstCell),
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
                    PuzzleMutationService(puzzle).toggleCandidate(cell.coordinates, candidate) {
                        PuzzleMessageBroker.message("$messagePrefix: $it")
                    }
                    AnalyzeResult.CandidatesEliminated
                } else AnalyzeResult.NoChanges
            }
        )

    private fun eliminateOtherCandidatesInBiChoiceCellPair(): AnalyzeResult {
        val biChoiceCellPairs =
            puzzle.allCellCollections()
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
                        if (puzzleTraverser.cellAt(coordinates).analysis.candidates.size > 2) {
                            PuzzleMutationService(puzzle).setCellCandidates(coordinates, mapEntry.value) {
                                PuzzleMessageBroker.message("Bi-choice cell candidates eliminated: $it")
                            }
                            AnalyzeResult.CandidatesEliminated
                        } else AnalyzeResult.NoChanges
                    }
                }
            )
        } else AnalyzeResult.NoChanges
    }
}
