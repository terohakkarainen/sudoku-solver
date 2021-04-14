package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.RevisionInformation
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.print.SudokuPrinter
import fi.thakki.sudokusolver.service.analyzer.HeuristicCandidateEliminator
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.util.SudokuTraverser
import kotlin.system.exitProcess

class GuessingSolver(private var sudoku: Sudoku) {

    class NoMoreBiChoiceCellsException(msg: String) : RuntimeException(msg)

    private var level = 0
    private var tests = 0L
    private val messageBroker = HeuristicCandidateEliminator.Companion.DiscardingMessageBroker
    private val revisionService = SudokuRevisionService().apply {
        newRevision(sudoku, "Initial revision").also { revisionInfo ->
            setCurrentRevisionInfo(revisionInfo)
        }
        SudokuPrinter(sudoku).printSudoku()
    }

    fun guess() {
        level += 1
        biChoiceCells().forEach { biChoiceCell ->
            val revisionBefore = currentRevisionNumber()
            doGuess(biChoiceCell)
            val revisionAfter = currentRevisionNumber()
            if (revisionBefore != revisionAfter) {
                sudoku = revisionService.restoreRevision(revisionBefore).sudoku
                require(currentRevisionNumber() == revisionBefore)
            }
        }
        printProgress()
        level -= 1
    }

    private fun doGuess(biChoiceCellCoords: Coordinates) {
        val oldRevision = currentRevisionNumber()

        val togglingSuccessful = if (toggleAndAnalyze(biChoiceCellCoords, true)) {
            true
        } else toggleAndAnalyze(biChoiceCellCoords, false)

        if (togglingSuccessful) {
            require(currentRevisionNumber() != oldRevision)
            // new revision so that we can return to this point.
            storeNewRevision()
            when {
                sudoku.state == Sudoku.State.COMPLETE -> handleCompleted()
                biChoiceCells().isNotEmpty() -> guess()
                else -> throw NoMoreBiChoiceCellsException("No more bi-choice cells")
            }
        }
    }

    private fun toggleAndAnalyze(biChoiceCellCoords: Coordinates, first: Boolean): Boolean {
        try {
            tests += 1
            // new revision so that we can back off if constraint gets violated.
            storeNewRevision()
            val cell = SudokuTraverser(sudoku).cellAt(biChoiceCellCoords)
            require(cell.analysis.candidates.size == 2)
            SudokuMutationService(sudoku).toggleCandidate(
                biChoiceCellCoords,
                if (first) {
                    cell.analysis.candidates.first()
                } else {
                    cell.analysis.candidates.last()
                }
            )
            SudokuAnalyzer(sudoku, messageBroker).analyze(rounds = Int.MAX_VALUE, doHeuristicAnalysis = false)
            return true
        } catch (e: SudokuConstraintViolationException) {
            sudoku = revisionService.restorePreviousRevision().sudoku
            return false
        }
    }

    private fun biChoiceCells(): Set<Coordinates> =
        sudoku.cells.cellsWithoutValue()
            .filter { it.analysis.candidates.size == 2 }
            .map { it.coordinates }
            .shuffled()
            .toSet()

    private fun storeNewRevision() {
        setCurrentRevisionInfo(
            revisionService.newRevision(sudoku, "Guess at level $level")
        )
    }

    private fun handleCompleted() {
        println("Complete!")
        SudokuPrinter(sudoku).printSudoku()
        exitProcess(0)
    }

    private fun printProgress() {
        println("*".repeat(level * 2) + " ($level, $tests)")
    }

    private fun currentRevisionNumber() =
        checkNotNull(sudoku.revisionInformation).number

    private fun setCurrentRevisionInfo(revisionInfo: RevisionInformation) {
        sudoku.revisionInformation = revisionInfo
    }
}
