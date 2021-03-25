package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser

class CellValueDeducer(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

    data class DeducedValue(
        val coordinates: Coordinates,
        val value: Symbol
    )

    fun deduceSomeValue(): AnalyzeResult {
        findCellWithOnlyOneCandidate(puzzle.cells.cellsWithoutValue())?.let { deducedValue ->
            return changeValue(deducedValue, "Only one candidate in cell")
        }

        puzzle.allCellCollections()
            .forEach { cellCollection ->
                findCellWithOnlyCandidateInCollection(cellCollection)?.let { deducedValue ->
                    return changeValue(
                        deducedValue,
                        "Only one candidate in ${cellCollection::class.simpleName?.toLowerCase()}"
                    )
                }
            }

        return AnalyzeResult.NoChanges
    }

    private fun changeValue(deducedValue: DeducedValue, messagePrefix: String): AnalyzeResult {
        PuzzleMutationService(puzzle).setCellValue(deducedValue.coordinates, deducedValue.value) { message ->
            messageBroker.message("$messagePrefix: $message")
        }
        PuzzleTraverser(puzzle).let { puzzleTraverser ->
            puzzleTraverser.cellAt(deducedValue.coordinates).let { changedCell ->
                listOf(
                    puzzleTraverser.regionOf(changedCell),
                    puzzleTraverser.bandOf(changedCell),
                    puzzleTraverser.stackOf(changedCell)
                ).map { cellCollection ->
                    cellCollection.cellsWithoutValue()
                }.reduce { acc, cellCollection -> acc.union(cellCollection) }
                    .minus(changedCell)
                    .forEach { cell ->
                        PuzzleMutationService(puzzle).removeCandidate(cell.coordinates, deducedValue.value) { message ->
                            messageBroker.message("Removed candidate due to value set: $message")
                        }
                    }
            }
        }
        return AnalyzeResult.ValueSet(deducedValue.value, deducedValue.coordinates)
    }

    private fun findCellWithOnlyOneCandidate(cells: Collection<Cell>): DeducedValue? =
        cells.find { it.analysis.candidates.size == 1 }?.let { cell ->
            DeducedValue(cell.coordinates, cell.analysis.candidates.single())
        }

    private fun findCellWithOnlyCandidateInCollection(cells: CellCollection): DeducedValue? =
        cells.cellsWithoutValue().let { unsolvedCells ->
            puzzle.symbols.forEach { symbol ->
                unsolvedCells.singleOrNull { cell -> cell.analysis.candidates.contains(symbol) }?.let { cellToSet ->
                    return DeducedValue(cellToSet.coordinates, symbol)
                }
            }
            null
        }
}
