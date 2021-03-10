package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkCandidateEliminator(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun eliminateCandidates(): AnalyzeResult =
        StrongLinkCandidateEliminator(puzzle).let { eliminator ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    eliminator.eliminateCandidatesUsingStrongLinksInRegions(),
                    eliminator.eliminateCandidatesInRegionsWithStrongLinksInBands(),
                    eliminator.eliminateCandidatesInRegionsWithStrongLinksInStacks()
                )
            )
        }

    private fun eliminateCandidatesUsingStrongLinksInRegions(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.regions.flatMap { region ->
                region.analysis.strongLinks.map { strongLink ->
                    when {
                        strongLink.firstCell.coordinates.x == strongLink.secondCell.coordinates.x ->
                            eliminateLinkCandidateFromOutsideCollection(
                                PuzzleTraverser(puzzle)::stackOf,
                                strongLink,
                                region
                            )
                        strongLink.firstCell.coordinates.y == strongLink.secondCell.coordinates.y ->
                            eliminateLinkCandidateFromOutsideCollection(
                                PuzzleTraverser(puzzle)::bandOf,
                                strongLink,
                                region
                            )
                        else -> AnalyzeResult.NoChanges
                    }
                }
            }
        )

    private fun eliminateLinkCandidateFromOutsideCollection(
        outsideTraverserFunc: (Cell) -> Collection<Cell>,
        link: StrongLink,
        excludedCells: Collection<Cell>
    ): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            outsideTraverserFunc(link.firstCell).minus(excludedCells).map { cell ->
                if (cell.type == CellValueType.SETTABLE &&
                    cell.analysis.candidates.contains(link.symbol)
                ) {
                    PuzzleMutationService(puzzle).setCellCandidates(
                        cell.coordinates,
                        cell.analysis.candidates.minus(link.symbol)
                    )
                    AnalyzeResult.CandidatesEliminated
                } else AnalyzeResult.NoChanges
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInBands(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.bands.flatMap { band ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(band)
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInStacks(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.stacks.flatMap { stack ->
                eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(stack)
            }
        )

    private fun eliminateCandidatesInRegionsWithStrongLinksInBandOrStack(
        cellCollection: CellCollection
    ): List<AnalyzeResult> =
        cellCollection.analysis.strongLinks.map { strongLink ->
            when {
                puzzleTraverser.regionOf(strongLink.firstCell) ==
                        puzzleTraverser.regionOf(strongLink.secondCell) ->
                    eliminateLinkCandidateFromOutsideCollection(
                        PuzzleTraverser(puzzle)::regionOf,
                        strongLink,
                        cellCollection.cells
                    )
                else -> AnalyzeResult.NoChanges
            }
        }
}
