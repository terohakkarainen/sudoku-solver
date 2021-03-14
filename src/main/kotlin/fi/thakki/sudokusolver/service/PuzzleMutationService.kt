package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
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
        checkCallCanBeSet(coordinates, value, false)
        puzzleTraverser.cellAt(coordinates).setGiven(value)
        PuzzleMessageBroker.message("Cell $coordinates value given as $value")
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        checkCallCanBeSet(coordinates, value, true)
        puzzleTraverser.cellAt(coordinates).apply {
            this.value = value
        }
        PuzzleMessageBroker.message("Cell $coordinates value set to $value")
    }

    fun resetCell(coordinates: Coordinates) {
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleTraverser.cellAt(coordinates).value = null
        PuzzleMessageBroker.message("Cell $coordinates value reset")
    }

    fun setCellCandidates(coordinates: Coordinates, candidates: Set<Symbol>) {
        candidates.forEach { candidate ->
            checkCallCanBeSet(coordinates, candidate, false)
        }
        puzzleTraverser.cellAt(coordinates).analysis.candidates = candidates
        PuzzleMessageBroker.message("Cell $coordinates candidates set to $candidates")
    }

    fun addStrongLink(
        cellCollection: CellCollection,
        candidate: Symbol,
        firstCell: Cell,
        secondCell: Cell,
        strongLinkType: StrongLinkType
    ) {
        listOf(firstCell.coordinates, secondCell.coordinates).forEach { coordinates ->
            checkCallCanBeSet(coordinates, candidate, false)
        }
        puzzleConstraintChecker.checkCellsApplicableForStrongLink(candidate, firstCell, secondCell, strongLinkType)

        StrongLink(candidate, firstCell, secondCell).also { strongLink ->
            cellCollection.analysis.strongLinks += strongLink
            firstCell.analysis.strongLinks += strongLink
            secondCell.analysis.strongLinks += strongLink
        }

        PuzzleMessageBroker.message(
            "Created $strongLinkType strong link between " +
                    "cell ${firstCell.coordinates} and cell ${secondCell.coordinates} for candidate $candidate"
        )
    }

    fun toggleCandidate(coordinates: Coordinates, value: Symbol) {
        checkCallCanBeSet(coordinates, value, false)

        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.analysis.candidates.contains(value)) {
                cell.analysis.candidates -= value
                PuzzleMessageBroker.message("Candidate $value removed from cell $coordinates")
            } else {
                cell.analysis.candidates += value
                PuzzleMessageBroker.message("Candidate $value added to cell $coordinates")
            }
        }
    }

    private fun checkCallCanBeSet(coordinates: Coordinates, value: Symbol, canBeAlreadySet: Boolean) {
        puzzleConstraintChecker.checkSymbolIsSupported(value)
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        if (!canBeAlreadySet) {
            puzzleConstraintChecker.checkCellIsNotSet(coordinates)
        }
        puzzleConstraintChecker.checkValueIsLegal(coordinates, value)
    }
}
