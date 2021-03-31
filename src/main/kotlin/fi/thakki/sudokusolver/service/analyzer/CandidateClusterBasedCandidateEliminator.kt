package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService
import kotlin.math.roundToInt

data class CandidateCluster(
    val candidates: Set<Symbol>,
    val cells: Set<Cell>
)

class CandidateClusterBasedCandidateEliminator(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
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
                                PuzzleMutationService(puzzle).setCellCandidates(
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
        clusterSizesForPuzzle()
            .takeIf { sizes -> sizes.isNotEmpty() }?.let { sizes ->
                sizes
                    .map { size -> findClustersOfSize(size) }
                    .reduce { acc, clusters -> acc.union(clusters) }
            } ?: emptySet()

    internal fun clusterSizesForPuzzle(): Set<Int> =
        (puzzle.dimension.value.toDouble() / 2f).roundToInt().let { greatestSize ->
            (SMALLEST_CLUSTER_SIZE..greatestSize).toSet()
        }

    private fun findClustersOfSize(clusterSize: Int): Set<CandidateCluster> {
        val results = mutableSetOf<CandidateCluster>()

        puzzle.allCellCollections().forEach { cellCollection ->
            val clusterFrequencyBySymbol =
                puzzle.symbols
                    .map { symbol ->
                        symbol to cellCollection.cellsWithoutValue().count { cell ->
                            cell.analysis.candidates.contains(symbol)
                        }
                    }
                    .toMap()
                    .filterValues { frequency -> frequency in 1..clusterSize }

            val symbolPermutations = permutations(clusterFrequencyBySymbol.keys, clusterSize)

            val affectedCellsBySymbols = symbolPermutations.map { symbolPermutation ->
                symbolPermutation to cellCollection.cellsWithoutValue()
                    .filter { cell ->
                        cell.analysis.candidates.any { candidate ->
                            candidate in symbolPermutation
                        }
                    }.toSet()
            }
                .toMap()
                .filterValues { cells -> cells.size == clusterSize }

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
