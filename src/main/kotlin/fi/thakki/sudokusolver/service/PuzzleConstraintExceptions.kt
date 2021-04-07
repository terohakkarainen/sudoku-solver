package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol

abstract class SudokuConstraintViolationException(message: String) : RuntimeException(message)

class SymbolNotSupportedException(symbol: Symbol) :
    SudokuConstraintViolationException("Symbol '$symbol' is not supported by sudoku")

class GivenCellNotModifiableException(cell: Cell) :
    SudokuConstraintViolationException("Cell at ${cell.coordinates} is given")

class CellValueSetException(cell: Cell) :
    SudokuConstraintViolationException("Cell at ${cell.coordinates} is already set")

class SudokuInvariantViolationException(cellCollection: CellCollection, symbol: Symbol) :
    SudokuConstraintViolationException(
        "Sudoku invariant does not hold for symbol $symbol in ${cellCollection::class.simpleName?.toLowerCase()} " +
                "with first cell at ${cellCollection.cells.first().coordinates}"
    )

class SymbolInUseException(symbol: Symbol, cell: Cell, symbolLocation: SudokuMutationService.SymbolLocation) :
    SudokuConstraintViolationException(
        "Cell at ${cell.coordinates} cannot be set, symbol " +
                "'$symbol' already in use in ${symbolLocation.name.toLowerCase()}"
    )

class StronglyLinkedCellsNotInSameCollectionException(
    firstCell: Cell,
    secondCell: Cell,
    strongLinkType: StrongLinkType
) :
    SudokuConstraintViolationException(
        "Cells ${firstCell.coordinates} and ${secondCell.coordinates} " +
                "are not in same collection of type $strongLinkType"
    )

class StronglyLinkedCellsDoNotContainCandidateException(firstCell: Cell, secondCell: Cell, candidate: Symbol) :
    SudokuConstraintViolationException(
        "Cells ${firstCell.coordinates} and ${secondCell.coordinates} do not contain candidate $candidate"
    )
