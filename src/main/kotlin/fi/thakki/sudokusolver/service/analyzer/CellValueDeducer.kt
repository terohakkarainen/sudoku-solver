package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.service.SudokuMutationService
import fi.thakki.sudokusolver.util.SudokuTraverser

class CellValueDeducer(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    data class DeducedValue(
        val coordinates: Coordinates,
        val value: Symbol
    )

    fun deduceSomeValue(): AnalyzeResult {
        findCellWithOnlyOneCandidate(sudoku.cells.cellsWithoutValue())?.let { deducedValue ->
            return changeValue(deducedValue, "Only one candidate in cell")
        }

        sudoku.allCellCollections()
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
        SudokuMutationService(sudoku).setCellValue(deducedValue.coordinates, deducedValue.value) { message ->
            messageBroker.message("$messagePrefix: $message")
        }
        SudokuTraverser(sudoku).let { sudokuTraverser ->
            sudokuTraverser.cellAt(deducedValue.coordinates).let { changedCell ->
                listOf(
                    sudokuTraverser.regionOf(changedCell),
                    sudokuTraverser.bandOf(changedCell),
                    sudokuTraverser.stackOf(changedCell)
                ).map { cellCollection ->
                    cellCollection.cellsWithoutValue()
                }.reduce { acc, cellCollection -> acc.union(cellCollection) }
                    .minus(changedCell)
                    .forEach { cell ->
                        SudokuMutationService(sudoku).removeCandidate(cell.coordinates, deducedValue.value) { message ->
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
            sudoku.symbols.forEach { symbol ->
                unsolvedCells.singleOrNull { cell -> cell.analysis.candidates.contains(symbol) }?.let { cellToSet ->
                    return DeducedValue(cellToSet.coordinates, symbol)
                }
            }
            null
        }
}
