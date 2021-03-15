package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService

class CellValueDeducer(private val puzzle: Puzzle) {

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
        PuzzleMutationService(puzzle).setCellValue(deducedValue.coordinates, deducedValue.value) {
            PuzzleMessageBroker.message("$messagePrefix: $it")
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
