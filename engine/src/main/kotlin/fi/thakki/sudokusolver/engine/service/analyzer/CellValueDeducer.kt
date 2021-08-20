package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Cell
import fi.thakki.sudokusolver.engine.model.CellCollection
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService
import java.util.Locale

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
                        "Only one candidate in ${cellCollection::class.simpleName?.lowercase(Locale.getDefault())}"
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
                    .forEach { cell: Cell ->
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
