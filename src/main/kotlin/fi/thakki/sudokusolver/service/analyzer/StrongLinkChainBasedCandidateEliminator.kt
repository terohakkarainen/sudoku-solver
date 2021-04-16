package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.service.mutation.SudokuMutationService
import fi.thakki.sudokusolver.service.SudokuTraverser

class StrongLinkChainBasedCandidateEliminator(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private val sudokuTraverser = SudokuTraverser(sudoku)

    fun eliminateCandidates(): AnalyzeResult =
        runEagerly(this::eliminateCandidatesUsingStrongLinkChains)

    private fun eliminateCandidatesUsingStrongLinkChains(): AnalyzeResult {
        var result: AnalyzeResult = AnalyzeResult.NoChanges

        sudoku.symbols.forEach { symbol ->
            sudoku.analysis.strongLinkChains
                .filter { it.symbol == symbol }
                .map { linkChain ->
                    sudokuTraverser.intersectionsOf(linkChain.first().firstCell, linkChain.last().secondCell)
                }.flatMap { cellPair ->
                    when {
                        sudokuTraverser.inSameStack(cellPair.first, cellPair.second) -> {
                            sudoku.stacks[cellPair.first.coordinates.x].cells.minus(
                                listOf(
                                    cellPair.first,
                                    cellPair.second
                                )
                            )
                        }
                        sudokuTraverser.inSameBand(cellPair.first, cellPair.second) -> {
                            sudoku.bands[cellPair.first.coordinates.y].cells.minus(
                                listOf(
                                    cellPair.first,
                                    cellPair.second
                                )
                            )
                        }
                        else -> listOf(cellPair.first, cellPair.second)
                    }
                }.toSet().map { cell ->
                    if (cell.analysis.candidates.contains(symbol)) {
                        SudokuMutationService(sudoku).removeCandidate(cell.coordinates, symbol) { message ->
                            messageBroker.message("Removed candidate with strong link chain: $message")
                        }
                        result = AnalyzeResult.CandidatesEliminated
                    }
                }
        }

        return result
    }
}
