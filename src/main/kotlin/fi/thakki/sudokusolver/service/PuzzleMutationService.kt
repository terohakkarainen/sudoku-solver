package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.extensions.containsSymbol
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

class PuzzleMutationService(private val puzzle: Puzzle) {

    enum class SymbolLocation {
        BAND,
        STACK,
        REGION
    }

    abstract class PuzzleMutationDeniedException(message: String) : RuntimeException(message)

    class SymbolNotSupportedException(symbol: Symbol) :
        PuzzleMutationDeniedException("Symbol '$symbol' is not supported by puzzle")

    class CellGivenException(cell: Cell) :
        PuzzleMutationDeniedException("Cell at ${cell.coordinates} is given")

    class SymbolInUseException(symbol: Symbol, cell: Cell, symbolLocation: SymbolLocation) :
        PuzzleMutationDeniedException(
            "Cell at ${cell.coordinates} cannot be set, symbol " +
                    "'$symbol' already in use in ${symbolLocation.name.toLowerCase()}"
        )

    private val puzzleTraverser = PuzzleTraverser(puzzle)

    fun setCellGiven(coordinates: Coordinates, value: Symbol) {
        checkSymbolIsSupported(value)
        checkCellIsNotGiven(coordinates)
        checkNewValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).setGiven(value)
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        checkSymbolIsSupported(value)
        checkCellIsNotGiven(coordinates)
        checkNewValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).value = value
    }

    fun resetCell(coordinates: Coordinates) {
        checkCellIsNotGiven(coordinates)
        puzzleTraverser.cellAt(coordinates).value = null
    }

    private fun checkSymbolIsSupported(symbol: Symbol) {
        if (symbol !in puzzle.symbols) {
            throw SymbolNotSupportedException(symbol)
        }
    }

    private fun checkCellIsNotGiven(coordinates: Coordinates) =
        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.type == CellValueType.GIVEN) {
                throw CellGivenException(cell)
            }
        }

    private fun checkNewValueIsLegal(coordinates: Coordinates, newValue: Symbol) {
        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.value != newValue) {
                when {
                    puzzleTraverser.stackOf(cell).containsSymbol(newValue) -> SymbolLocation.STACK
                    puzzleTraverser.bandOf(cell).containsSymbol(newValue) -> SymbolLocation.BAND
                    puzzleTraverser.regionOf(cell).containsSymbol(newValue) -> SymbolLocation.REGION
                    else -> null
                }?.let { symbolLocation ->
                    throw SymbolInUseException(newValue, cell, symbolLocation)
                }
            }
        }
    }
}
