package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol

data class CandidateCluster(
    val cellCollection: CellCollection,
    val candidates: Set<Symbol>,
    val cells: Set<Cell>
)

class CandidateClusterFinder(private val puzzle: Puzzle) {

    @Suppress("MagicNumber")
    fun findClusters() {
        findClustersOfSize(3).union(findClustersOfSize(4)).forEach { cluster ->
            cluster.cellCollection.cellsWithoutValue().minus(cluster.cells).forEach { cell ->
                val removableCellCandidates = cell.analysis.candidates.intersect(cluster.candidates)
                if (removableCellCandidates.isNotEmpty()) {
                    println("*** Cluster could eliminate candidates $removableCellCandidates from cell $cell ***")
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun findClustersOfSize(clusterSize: Int): Set<CandidateCluster> {
        val result = mutableSetOf<CandidateCluster>()
        puzzle.allCellCollections().forEach { cellCollection ->
            puzzle.symbols.forEach { symbol ->
                val unsetCells = cellCollection.cellsWithoutValue()
                val cellsWithSymbolAsCandidate = unsetCells.filter { it.analysis.candidates.contains(symbol) }
                if (cellsWithSymbolAsCandidate.size <= clusterSize) {
                    val otherCandidatesInCells =
                        cellsWithSymbolAsCandidate
                            .flatMap { it.analysis.candidates }
                            .toSet()
                            .minus(symbol)
                    if (otherCandidatesInCells.size == clusterSize - 1) {
                        val candidateTriplet = setOf(symbol).union(otherCandidatesInCells)
                        val cells = unsetCells
                            .filter { it.analysis.candidates.intersect(candidateTriplet).isNotEmpty() }
                            .toSet()
                        if (cells.size == clusterSize && cells != unsetCells) {
                            result.add(CandidateCluster(cellCollection, candidateTriplet, cells))
                        }
                    }
                }
            }
        }
//        result.forEach { cluster ->
//            println("*** Found candidate cluster ${cluster.candidates} in cells ${cluster.cells} ***")
//        }
        return result
    }
}
