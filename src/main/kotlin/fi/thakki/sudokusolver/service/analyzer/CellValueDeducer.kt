package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.extensions.unsetCells
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMutationService

class CellValueDeducer(private val puzzle: Puzzle) {

    data class DeducedValue(
        val coordinates: Coordinates,
        val value: Symbol
    )

    fun deduceSomeValue(): AnalyzeResult {
        findCellWithOnlyOneCandidate(puzzle.cells.unsetCells())?.let { deducedValue ->
            return changeValue(deducedValue)
        }

        listOf(
            puzzle.bands.map { it.cells },
            puzzle.stacks.map { it.cells },
            puzzle.regions.map { it.cells }
        ).flatten().forEach { cellCollection ->
            findCellWithOnlyCandidateInCollection(cellCollection)?.let { deducedValue ->
                return changeValue(deducedValue)
            }
        }

        return AnalyzeResult.NoChanges
    }

    private fun changeValue(deducedValue: DeducedValue): AnalyzeResult {
        PuzzleMutationService(puzzle).setCellValue(deducedValue.coordinates, deducedValue.value)
        return AnalyzeResult.ValueSet(deducedValue.value, deducedValue.coordinates)
    }

    private fun findCellWithOnlyOneCandidate(cells: Collection<Cell>): DeducedValue? =
        cells.find { it.analysis.candidates.size == 1 }?.let { cell ->
            DeducedValue(cell.coordinates, cell.analysis.candidates.single())
        }

    private fun findCellWithOnlyCandidateInCollection(cells: Collection<Cell>): DeducedValue? =
        cells.unsetCells().let { unsolvedCells ->
            puzzle.symbols.forEach { symbol ->
                unsolvedCells.singleOrNull { cell -> cell.analysis.candidates.contains(symbol) }?.let { cellToSet ->
                    return DeducedValue(cellToSet.coordinates, symbol)
                }
            }
            null
        }
}
