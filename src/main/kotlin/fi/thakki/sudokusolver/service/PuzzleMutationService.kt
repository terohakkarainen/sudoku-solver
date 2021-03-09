package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

class PuzzleMutationService(puzzle: Puzzle) {

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
        puzzleConstraintChecker.checkValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).setGiven(value)
        PuzzleMessageBroker.message("Cell $coordinates value given as $value")
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        puzzleConstraintChecker.checkSymbolIsSupported(value)
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleConstraintChecker.checkValueIsLegal(coordinates, value)
        puzzleTraverser.cellAt(coordinates).apply {
            this.value = value
            this.analysis.clear()
        }
        PuzzleMessageBroker.message("Cell $coordinates value set to $value")
    }

    fun resetCell(coordinates: Coordinates) {
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleTraverser.cellAt(coordinates).value = null
        PuzzleMessageBroker.message("Cell $coordinates value reset")
    }

    fun setCellCandidates(coordinates: Coordinates, candidates: Set<Symbol>) {
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        candidates.forEach { candidate ->
            puzzleConstraintChecker.checkSymbolIsSupported(candidate)
            puzzleConstraintChecker.checkValueIsLegal(coordinates, candidate)
        }
        puzzleTraverser.cellAt(coordinates).let { cell ->
            cell.analysis.candidates.clear()
            cell.analysis.candidates.addAll(candidates)
        }
        PuzzleMessageBroker.message("Cell $coordinates candidates set to $candidates")
    }

    fun addStrongLink(candidate: Symbol, firstCell: Cell, secondCell: Cell, strongLinkType: StrongLinkType) {
        puzzleConstraintChecker.checkSymbolIsSupported(candidate)
        puzzleConstraintChecker.checkCellIsNotGiven(firstCell.coordinates)
        puzzleConstraintChecker.checkCellIsNotGiven(secondCell.coordinates)
        puzzleConstraintChecker.checkCellIsNotSet(firstCell.coordinates)
        puzzleConstraintChecker.checkCellIsNotSet(secondCell.coordinates)
        puzzleConstraintChecker.checkValueIsLegal(firstCell.coordinates, candidate)
        puzzleConstraintChecker.checkValueIsLegal(secondCell.coordinates, candidate)
        puzzleConstraintChecker.checkCellsApplicableForStrongLink(firstCell, secondCell, strongLinkType)
        firstCell.analysis.strongLinks.add(StrongLink(candidate, secondCell, strongLinkType))
        secondCell.analysis.strongLinks.add(StrongLink(candidate, firstCell, strongLinkType))
        PuzzleMessageBroker.message(
            "Created $strongLinkType strong link between " +
                    "cell ${firstCell.coordinates} and cell ${secondCell.coordinates} for candidate $candidate"
        )
    }
}
