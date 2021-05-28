package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService
import fi.thakki.sudokusolver.engine.util.permutations
import kotlin.math.roundToInt

data class CandidateCluster(
    val candidates: Set<Symbol>,
    val cells: Set<Cell>
)

class CandidateClusterBasedCandidateEliminator(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    @Suppress("MagicNumber")
    fun eliminateCandidates(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            findClusters().flatMap { cluster ->
                cluster.cells.map { clusterCell ->
                    clusterCell.analysis.candidates.minus(cluster.candidates).let { removableCellCandidates ->
                        when {
                            removableCellCandidates.isEmpty() -> AnalyzeResult.NoChanges
                            else -> {
                                SudokuMutationService(sudoku).setCellCandidates(
                                    clusterCell.coordinates,
                                    clusterCell.analysis.candidates.minus(removableCellCandidates)
                                ) { message ->
                                    messageBroker.message(
                                        "Candidates $removableCellCandidates eliminated by cluster of " +
                                                "size ${cluster.candidates.size}: $message"
                                    )
                                }
                                AnalyzeResult.CandidatesEliminated
                            }
                        }
                    }
                }
            }
        )

    private fun findClusters(): Set<CandidateCluster> =
        clusterSizesForSudoku()
            .takeIf { sizes -> sizes.isNotEmpty() }?.let { sizes ->
                sizes
                    .map { size -> findClustersOfSize(size) }
                    .reduce { acc, clusters -> acc.union(clusters) }
            } ?: emptySet()

    internal fun clusterSizesForSudoku(): Set<Int> =
        (sudoku.dimension.value.toDouble() / 2f).roundToInt().let { greatestSize ->
            (SMALLEST_CLUSTER_SIZE..greatestSize).toSet()
        }

    private fun findClustersOfSize(clusterSize: Int): Set<CandidateCluster> {
        val results = mutableSetOf<CandidateCluster>()

        sudoku.allCellCollections().forEach { cellCollection ->
            val clusterFrequencyBySymbol =
                sudoku.symbols.associateWith { symbol ->
                    cellCollection.cellsWithoutValue().count { cell ->
                        cell.analysis.candidates.contains(symbol)
                    }
                }.filterValues { frequency -> frequency in 1..clusterSize }

            val symbolPermutations = permutations(clusterFrequencyBySymbol.keys, clusterSize)

            val affectedCellsBySymbols = symbolPermutations.associateWith { symbolPermutation ->
                cellCollection.cellsWithoutValue()
                    .filter { cell ->
                        cell.analysis.candidates.any { candidate ->
                            candidate in symbolPermutation
                        }
                    }.toSet()
            }.filterValues { cells -> cells.size == clusterSize }

            results.addAll(
                affectedCellsBySymbols.entries.map { entry ->
                    CandidateCluster(entry.key, entry.value)
                }
            )
        }

        return results
    }

    companion object {
        const val SMALLEST_CLUSTER_SIZE = 3
    }
}
