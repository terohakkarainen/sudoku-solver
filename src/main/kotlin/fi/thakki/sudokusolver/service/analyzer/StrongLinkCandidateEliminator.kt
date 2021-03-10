package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkCandidateEliminator(private val puzzle: Puzzle) {

    fun eliminateCandidatesUsingStrongLinksInRegions(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            puzzle.regions.flatMap { region ->
                region.analysis.strongLinks.map { strongLink ->
                    when {
                        strongLink.firstCell.coordinates.x == strongLink.secondCell.coordinates.x ->
                            eliminateLinkCandidateFromOutsideRegion(
                                PuzzleTraverser(puzzle)::stackOf,
                                strongLink,
                                region
                            )
                        strongLink.firstCell.coordinates.y == strongLink.secondCell.coordinates.y ->
                            eliminateLinkCandidateFromOutsideRegion(
                                PuzzleTraverser(puzzle)::bandOf,
                                strongLink,
                                region
                            )
                        else -> AnalyzeResult.NoChanges
                    }
                }
            }
        )

    private fun eliminateLinkCandidateFromOutsideRegion(
        traverserFunc: (Cell) -> Collection<Cell>,
        link: StrongLink,
        region: Region
    ): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            traverserFunc(link.firstCell).minus(region).map { cell ->
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
}
