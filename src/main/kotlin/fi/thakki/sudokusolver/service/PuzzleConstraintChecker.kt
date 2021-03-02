package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.extensions.containsSymbol
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
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

    fun checkNewValueIsLegal(coordinates: Coordinates, newValue: Symbol) {
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
}
