package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.SudokuTraverser

typealias MessageConsumer = (String) -> Unit

class SudokuMutationService(private val sudoku: Sudoku) {

    enum class SymbolLocation {
        BAND,
        STACK,
        REGION
    }

    private val sudokuTraverser = SudokuTraverser(sudoku)
    private val sudokuConstraintChecker = SudokuConstraintChecker(sudoku)

    fun setCellGiven(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCellCanBeSet(coordinates, value, false)
        sudokuTraverser.cellAt(coordinates).setGiven(value)
        sudokuConstraintChecker.checkSudokuInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value given as $value")
        }
    }

    fun setCellValue(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCellCanBeSet(coordinates, value, true)
        sudokuTraverser.cellAt(coordinates).apply {
            this.value = value
        }
        sudokuConstraintChecker.checkSudokuInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value set to $value")
        }
        if (sudoku.cells.cellsWithoutValue().isEmpty()) {
            sudoku.state = Sudoku.State.COMPLETE
            messageConsumer?.let { consumer ->
                consumer("sudoku is now complete")
            }
        }
    }

    fun resetCell(
        coordinates: Coordinates,
        messageConsumer: MessageConsumer? = null
    ) {
        sudokuConstraintChecker.checkCellIsNotGiven(coordinates)
        sudokuTraverser.cellAt(coordinates).value = null
        sudokuConstraintChecker.checkSudokuInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value reset")
        }
    }

    fun setCellCandidates(
        coordinates: Coordinates,
        candidates: Set<Symbol>,
        messageConsumer: MessageConsumer? = null
    ) {
        require(candidates.isNotEmpty()) { "Candidates cannot be set to an empty set" }
        candidates.forEach { candidate ->
            checkCellCanBeSet(coordinates, candidate, false)
        }
        sudokuTraverser.cellAt(coordinates).analysis.candidates = candidates
        sudokuConstraintChecker.checkSudokuInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates candidates set to $candidates")
        }
    }

    fun addStrongLink(
        cellCollection: CellCollection,
        candidate: Symbol,
        firstCell: Cell,
        secondCell: Cell,
        strongLinkType: StrongLinkType,
        messageConsumer: MessageConsumer? = null
    ) {
        listOf(firstCell.coordinates, secondCell.coordinates).forEach { coordinates ->
            checkCellCanBeSet(coordinates, candidate, false)
        }
        sudokuConstraintChecker.checkCellsApplicableForStrongLink(candidate, firstCell, secondCell, strongLinkType)

        StrongLink(candidate, firstCell, secondCell).also { strongLink ->
            cellCollection.analysis.strongLinks += strongLink
            firstCell.analysis.strongLinks += strongLink
            secondCell.analysis.strongLinks += strongLink
            messageConsumer?.let { consumer ->
                consumer("strong link $strongLink of type $strongLinkType created")
            }
        }
    }

    fun toggleCandidate(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCellCanBeSet(coordinates, value, false)

        sudokuTraverser.cellAt(coordinates).let { cell ->
            if (cell.analysis.candidates.contains(value)) {
                cell.analysis.candidates -= value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value removed from cell $coordinates")
                }
            } else {
                cell.analysis.candidates += value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value added to cell $coordinates")
                }
            }
        }
        sudokuConstraintChecker.checkSudokuInvariantHolds()
    }

    fun removeCandidate(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        sudokuConstraintChecker.checkSymbolIsSupported(value)
        sudokuConstraintChecker.checkCellIsNotGiven(coordinates)
        sudokuConstraintChecker.checkCellIsNotSet(coordinates)

        sudokuTraverser.cellAt(coordinates).let { cell ->
            if (cell.analysis.candidates.contains(value)) {
                cell.analysis.candidates -= value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value removed from cell $coordinates")
                }
            }
        }
        sudokuConstraintChecker.checkSudokuInvariantHolds()
    }

    private fun checkCellCanBeSet(coordinates: Coordinates, value: Symbol, canBeAlreadySet: Boolean) {
        sudokuConstraintChecker.checkSymbolIsSupported(value)
        sudokuConstraintChecker.checkCellIsNotGiven(coordinates)
        if (!canBeAlreadySet) {
            sudokuConstraintChecker.checkCellIsNotSet(coordinates)
        }
        sudokuConstraintChecker.checkValueIsLegal(coordinates, value)
    }
}
