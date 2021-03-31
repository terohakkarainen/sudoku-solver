package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class StrongLinkChainBasedCandidateEliminator(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(this::eliminateCandidatesUsingStrongLinkChains)

    private fun eliminateCandidatesUsingStrongLinkChains(): AnalyzeResult {
        var result: AnalyzeResult = AnalyzeResult.NoChanges

        puzzle.symbols.forEach { symbol ->
            puzzle.analysis.strongLinkChains
                .filter { it.symbol == symbol }
                .map { linkChain ->
                    puzzleTraverser.intersectionsOf(linkChain.first().firstCell, linkChain.last().secondCell)
                }.flatMap { cellPair ->
                    when {
                        puzzleTraverser.inSameStack(cellPair.first, cellPair.second) -> {
                            puzzle.stacks[cellPair.first.coordinates.x].cells.minus(
                                listOf(
                                    cellPair.first,
                                    cellPair.second
                                )
                            )
                        }
                        puzzleTraverser.inSameBand(cellPair.first, cellPair.second) -> {
                            puzzle.bands[cellPair.first.coordinates.y].cells.minus(
                                listOf(
                                    cellPair.first,
                                    cellPair.second
                                )
                            )
                        }
                        else -> {
                            listOf(cellPair.first, cellPair.second)
                        }
                    }
                }.toSet().map { cell ->
                    if (cell.analysis.candidates.contains(symbol)) {
                        PuzzleMutationService(puzzle).removeCandidate(cell.coordinates, symbol) { message ->
                            messageBroker.message("Removed candidate with strong link chain: $message")
                        }
                        result = AnalyzeResult.CandidatesEliminated
                    }
                }
        }

        return result
    }
}
