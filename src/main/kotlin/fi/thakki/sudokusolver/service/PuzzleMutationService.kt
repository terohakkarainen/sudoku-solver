package fi.thakki.sudokusolver.service

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

    private val puzzleTraverser = PuzzleTraverser(puzzle)
    private val puzzleConstraintChecker = PuzzleConstraintChecker(puzzle)

    fun setCellGiven(coordinates: Coordinates, value: Symbol) {
        puzzleConstraintChecker.checkSymbolIsSupported(value)
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleConstraintChecker.checkNewValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).setGiven(value)
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        puzzleConstraintChecker.checkSymbolIsSupported(value)
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleConstraintChecker.checkNewValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).value = value
    }

    fun resetCell(coordinates: Coordinates) {
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleTraverser.cellAt(coordinates).value = null
    }

    fun analyzeCells() {
        puzzle.cells.forEach { cell ->
            if (cell.value == null) {
                PuzzleAnalyzer(puzzle).analyze(cell)
            }
        }
    }
}
