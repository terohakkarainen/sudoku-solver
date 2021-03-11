package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

class PuzzleConstraintChecker(private val puzzle: Puzzle) {

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun checkSymbolIsSupported(symbol: Symbol) {
        if (symbol !in puzzle.symbols) {
            throw SymbolNotSupportedException(symbol)
        }
    }

    fun checkCellIsNotGiven(coordinates: Coordinates) =
        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.type == CellValueType.GIVEN) {
                throw GivenCellNotModifiableException(cell)
            }
        }

    fun checkCellIsNotSet(coordinates: Coordinates) =
        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.hasValue()) {
                throw CellValueSetException(cell)
            }
        }

    fun checkValueIsLegal(coordinates: Coordinates, newValue: Symbol) {
        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.value != newValue) {
                when {
                    puzzleTraverser.stackOf(cell).containsSymbol(newValue) ->
                        PuzzleMutationService.SymbolLocation.STACK
                    puzzleTraverser.bandOf(cell).containsSymbol(newValue) ->
                        PuzzleMutationService.SymbolLocation.BAND
                    puzzleTraverser.regionOf(cell).containsSymbol(newValue) ->
                        PuzzleMutationService.SymbolLocation.REGION
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
        strongLinkType: StrongLink.LinkType
    ) {
        fun checkCellsInSameCollection(traverser: (Cell) -> CellCollection) {
            if (traverser(firstCell) != traverser(secondCell)) {
                throw StronglyLinkedCellsNotInSameCollectionException(firstCell, secondCell, strongLinkType)
            }
        }

        when (strongLinkType) {
            StrongLink.LinkType.BAND -> checkCellsInSameCollection(puzzleTraverser::bandOf)
            StrongLink.LinkType.STACK -> checkCellsInSameCollection(puzzleTraverser::stackOf)
            StrongLink.LinkType.REGION -> checkCellsInSameCollection(puzzleTraverser::regionOf)
        }

        if (!firstCell.analysis.candidates.contains(candidate) || !secondCell.analysis.candidates.contains(candidate)) {
            throw StronglyLinkedCellsDoNotContainCandidateException(firstCell, secondCell, candidate)
        }
    }
}
