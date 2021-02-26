package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.extensions.containsSymbol
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol

class PuzzleMutationService(private val puzzle: Puzzle) {

    fun setCellGiven(coordinates: Coordinates, value: Symbol) {
        checkSymbolIsSupported(value)
        checkCellIsUnassigned(coordinates)
        checkNewValueIsLegal(coordinates, value)
        puzzle.cellAt(coordinates).setGiven(value)
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        checkSymbolIsSupported(value)
        checkCellIsNotGiven(coordinates)
        checkNewValueIsLegal(coordinates, value)
        puzzle.cellAt(coordinates).value = value
    }

    fun resetCell(coordinates: Coordinates) {
        checkCellIsNotGiven(coordinates)
        puzzle.cellAt(coordinates).value = null
    }

    private fun checkSymbolIsSupported(symbol: Symbol) {
        if (symbol !in puzzle.symbols) {
            throw IllegalArgumentException("Symbol $symbol is not supported by puzzle")
        }
    }

    private fun checkCellIsNotGiven(coordinates: Coordinates) =
        puzzle.cellAt(coordinates).let { cell ->
            if (cell.type == CellValueType.GIVEN) {
                throw IllegalArgumentException("Cell is given")
            }
        }

    private fun checkCellIsUnassigned(coordinates: Coordinates) {
        puzzle.cellAt(coordinates).let { cell ->
            when {
                cell.type == CellValueType.GIVEN ->
                    throw IllegalArgumentException("Cell is already given")
                cell.value != null ->
                    throw IllegalArgumentException("Cell is not blank")
                else -> Unit
            }
        }
    }

    private fun checkNewValueIsLegal(coordinates: Coordinates, newValue: Symbol) {
        val cell = puzzle.cellAt(coordinates)

        if (cell.value != newValue) {
            when {
                puzzle.stackOf(cell).containsSymbol(newValue) ->
                    "Symbol already in use in stack"
                puzzle.bandOf(cell).containsSymbol(newValue) -> {
                    "Symbol already in use in band"
                }
                puzzle.regionOf(cell).containsSymbol(newValue) -> {
                    "Symbol already in use in region"
                }
                else -> null
            }?.let { errorMessage ->
                throw IllegalArgumentException(errorMessage)
            }
        }
    }
}
