package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol

abstract class PuzzleConstraintViolationException(message: String) : RuntimeException(message)

class SymbolNotSupportedException(symbol: Symbol) :
    PuzzleConstraintViolationException("Symbol '$symbol' is not supported by puzzle")

class GivenCellNotModifiableException(cell: Cell) :
    PuzzleConstraintViolationException("Cell at ${cell.coordinates} is given")

class CellValueSetException(cell: Cell) :
    PuzzleConstraintViolationException("Cell at ${cell.coordinates} is already set")

class SymbolInUseException(symbol: Symbol, cell: Cell, symbolLocation: PuzzleMutationService.SymbolLocation) :
    PuzzleConstraintViolationException(
        "Cell at ${cell.coordinates} cannot be set, symbol " +
                "'$symbol' already in use in ${symbolLocation.name.toLowerCase()}"
    )

class CellsNotApplicableForStrongLinking(firstCell: Cell, secondCell: Cell, strongLinkType: StrongLinkType) :
    PuzzleConstraintViolationException(
        "Cells ${firstCell.coordinates} and ${secondCell.coordinates} " +
                "not applicable for strong link of type $strongLinkType"
    )
