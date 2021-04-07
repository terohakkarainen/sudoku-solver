package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.SudokuTraverser

class SudokuConstraintChecker(private val sudoku: Sudoku) {

    private val sudokuTraverser = SudokuTraverser(sudoku)

    fun checkSymbolIsSupported(symbol: Symbol) {
        if (!sudoku.symbols.isSupported(symbol)) {
            throw SymbolNotSupportedException(symbol)
        }
    }

    fun checkCellIsNotGiven(coordinates: Coordinates) =
        sudokuTraverser.cellAt(coordinates).let { cell ->
            if (cell.type == CellValueType.GIVEN) {
                throw GivenCellNotModifiableException(cell)
            }
        }

    fun checkCellIsNotSet(coordinates: Coordinates) =
        sudokuTraverser.cellAt(coordinates).let { cell ->
            if (cell.hasValue()) {
                throw CellValueSetException(cell)
            }
        }

    fun checkValueIsLegal(coordinates: Coordinates, newValue: Symbol) {
        sudokuTraverser.cellAt(coordinates).let { cell ->
            if (cell.value != newValue) {
                when {
                    sudokuTraverser.stackOf(cell).containsSymbol(newValue) ->
                        SudokuMutationService.SymbolLocation.STACK
                    sudokuTraverser.bandOf(cell).containsSymbol(newValue) ->
                        SudokuMutationService.SymbolLocation.BAND
                    sudokuTraverser.regionOf(cell).containsSymbol(newValue) ->
                        SudokuMutationService.SymbolLocation.REGION
                    else -> null
                }?.let { symbolLocation ->
                    throw SymbolInUseException(newValue, cell, symbolLocation)
                }
            }
        }
    }

    fun checkCellsApplicableForStrongLink(
        candidate: Symbol,
        firstCell: Cell,
        secondCell: Cell,
        strongLinkType: StrongLinkType
    ) {
        fun checkCellsInSameCollection(traverser: (Cell) -> CellCollection) {
            if (traverser(firstCell) != traverser(secondCell)) {
                throw StronglyLinkedCellsNotInSameCollectionException(firstCell, secondCell, strongLinkType)
            }
        }

        when (strongLinkType) {
            StrongLinkType.BAND -> checkCellsInSameCollection(sudokuTraverser::bandOf)
            StrongLinkType.STACK -> checkCellsInSameCollection(sudokuTraverser::stackOf)
            StrongLinkType.REGION -> checkCellsInSameCollection(sudokuTraverser::regionOf)
        }

        if (!firstCell.analysis.candidates.contains(candidate) || !secondCell.analysis.candidates.contains(candidate)) {
            throw StronglyLinkedCellsDoNotContainCandidateException(firstCell, secondCell, candidate)
        }
    }

    fun checkSudokuInvariantHolds() {
        sudoku.allCellCollections().forEach { cellCollection ->
            sudoku.symbols.forEach { symbol ->
                val valueCount = cellCollection.cells.count { cell -> cell.value == symbol }
                if (!(valueCount == 1 || (valueCount == 0 && cellCollection.cells.any { cell ->
                        cell.analysis.candidates.contains(symbol)
                    }))) {
                    throw SudokuInvariantViolationException(cellCollection, symbol)
                }
            }
        }
    }
}
