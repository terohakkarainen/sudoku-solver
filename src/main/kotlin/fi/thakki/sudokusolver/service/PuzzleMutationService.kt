package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.CellCollection
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLink
import fi.thakki.sudokusolver.model.StrongLinkType
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

typealias MessageConsumer = (String) -> Unit

class PuzzleMutationService(private val puzzle: Puzzle) {

    enum class SymbolLocation {
        BAND,
        STACK,
        REGION
    }

    private val puzzleTraverser = PuzzleTraverser(puzzle)
    private val puzzleConstraintChecker = PuzzleConstraintChecker(puzzle)

    fun setCellGiven(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCallCanBeSet(coordinates, value, false)
        puzzleTraverser.cellAt(coordinates).setGiven(value)
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value given as $value")
        }
    }

    fun setCellValue(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCallCanBeSet(coordinates, value, true)
        puzzleTraverser.cellAt(coordinates).apply {
            this.value = value
        }
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value set to $value")
        }
        if (puzzle.cells.cellsWithoutValue().isEmpty()) {
            puzzle.state = Puzzle.State.COMPLETE
            messageConsumer?.let { consumer ->
                consumer("puzzle is now complete")
            }
        }
    }

    fun resetCell(
        coordinates: Coordinates,
        messageConsumer: MessageConsumer? = null
    ) {
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleTraverser.cellAt(coordinates).value = null
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates value reset")
        }
    }

    fun setCellCandidates(
        coordinates: Coordinates,
        candidates: Set<Symbol>,
        messageConsumer: MessageConsumer? = null
    ) {
        require(candidates.isNotEmpty()) { "Candidates cannot be set to an empty set" }
        candidates.forEach { candidate ->
            checkCallCanBeSet(coordinates, candidate, false)
        }
        puzzleTraverser.cellAt(coordinates).analysis.candidates = candidates
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
        messageConsumer?.let { consumer ->
            consumer("cell $coordinates candidates set to $candidates")
        }
    }

    fun addStrongLink(
        cellCollection: CellCollection,
        candidate: Symbol,
        firstCell: Cell,
        secondCell: Cell,
        strongLinkType: StrongLinkType,
        messageConsumer: MessageConsumer? = null
    ) {
        listOf(firstCell.coordinates, secondCell.coordinates).forEach { coordinates ->
            checkCallCanBeSet(coordinates, candidate, false)
        }
        puzzleConstraintChecker.checkCellsApplicableForStrongLink(candidate, firstCell, secondCell, strongLinkType)

        StrongLink(candidate, firstCell, secondCell).also { strongLink ->
            cellCollection.analysis.strongLinks += strongLink
            firstCell.analysis.strongLinks += strongLink
            secondCell.analysis.strongLinks += strongLink
            messageConsumer?.let { consumer ->
                consumer("strong link $strongLink of type $strongLinkType created")
            }
        }
    }

    fun toggleCandidate(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        checkCallCanBeSet(coordinates, value, false)

        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.analysis.candidates.contains(value)) {
                cell.analysis.candidates -= value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value removed from cell $coordinates")
                }
            } else {
                cell.analysis.candidates += value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value added to cell $coordinates")
                }
            }
        }
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
    }

    // TODO add tests for method.
    fun removeCandidate(
        coordinates: Coordinates,
        value: Symbol,
        messageConsumer: MessageConsumer? = null
    ) {
        puzzleConstraintChecker.checkSymbolIsSupported(value)
        puzzleConstraintChecker.checkCellIsNotGiven(coordinates)
        puzzleConstraintChecker.checkCellIsNotSet(coordinates)

        puzzleTraverser.cellAt(coordinates).let { cell ->
            if (cell.analysis.candidates.contains(value)) {
                cell.analysis.candidates -= value
                messageConsumer?.let { consumer ->
                    consumer("candidate $value removed from cell $coordinates")
                }
            }
        }
        puzzleConstraintChecker.checkPuzzleInvariantHolds()
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
