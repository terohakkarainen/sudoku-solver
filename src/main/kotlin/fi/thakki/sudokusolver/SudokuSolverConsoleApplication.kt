package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleConstraintChecker
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleRevisionService
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlin.system.exitProcess

class SudokuSolverConsoleApplication(puzzleFileName: String) {

    private var puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFileName)
    private val quitPattern = Regex("^q$")
    private val printPattern = Regex("^p$")
    private val setPattern = Regex("^s ([0-9]*),([0-9]*) (.)$")
    private val resetPattern = Regex("^r ([0-9]*),([0-9]*)$")
    private val analyzePattern = Regex("^a( [0-9]*)?$")
    private val highlightPattern = Regex("^h (.)$")
    private val updateCandidatesPattern = Regex("^u$")
    private val updateStrongLinksPattern = Regex("^l$")
    private val eliminateCandidatesPattern = Regex("^e$")
    private val deduceValuesPattern = Regex("^d$")
    private val toggleCandidatePattern = Regex("^t ([0-9]*),([0-9]*) (.)$")
    private val undoPattern = Regex("^z$")

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        PuzzleRevisionService.newRevision(puzzle).also { newRevision ->
            PuzzleMessageBroker.message("Puzzle initialized, starting game with revision $newRevision")
        }
        while (true) {
            try {
                exitIfComplete()
                translateUserInputToCommand()
            } catch (e: Exception) {
                PuzzleMessageBroker.error("Error: ${e.message}")
            }
        }
    }

    private fun exitIfComplete() {
        if (puzzle.isComplete()) {
            PuzzleMessageBroker.message("Puzzle complete!")
            exitProcess(0)
        }
    }

    private fun translateUserInputToCommand() {
        printPrompt()
        val input = readLine()?.trim()?.toLowerCase()
        when {
            input == null -> unknownCommandError()
            quitPattern.matches(input) -> exitApplication()
            printPattern.matches(input) -> printPuzzle()
            setPattern.matches(input) -> setCellValue(input)
            resetPattern.matches(input) -> resetCell(input)
            analyzePattern.matches(input) -> analyzePuzzle(input)
            highlightPattern.matches(input) -> printPuzzleWithHighlighting(input)
            updateCandidatesPattern.matches(input) -> updateCandidates()
            updateStrongLinksPattern.matches(input) -> updateStrongLinks()
            eliminateCandidatesPattern.matches(input) -> eliminateCandidates()
            deduceValuesPattern.matches(input) -> deduceValues()
            toggleCandidatePattern.matches(input) -> toggleCandidate(input)
            undoPattern.matches(input) -> undo()
            else -> unknownCommandError()
        }
    }

    private fun printPrompt() {
        PuzzleMessageBroker.message(
            "SudokuSolver | q > quit | p > print | a_nn > analyze | s_x,y_v > set | r_x,y > reset | h_s > highlight"
        )
        PuzzleMessageBroker.message(
            "------------------------------------------------------------------------------------------------------"
        )
        PuzzleMessageBroker.message("Enter command: ", putLineFeed = false)
    }

    private fun unknownCommandError() {
        PuzzleMessageBroker.error("unknown command")
    }

    private fun exitApplication() {
        exitProcess(0)
    }

    private fun printPuzzle() {
        SudokuPrinter(puzzle).printPuzzle()
    }

    private fun printPuzzleWithHighlighting(input: String) {
        highlightPattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val symbol = value.first()
            PuzzleConstraintChecker(puzzle).checkSymbolIsSupported(symbol)
            SudokuPrinter(puzzle).printPuzzle(highlightedSymbol = symbol)
        }
    }

    private fun setCellValue(input: String) {
        setPattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            executeAndRevision(
                SetCellValueCommand(Coordinates(x.toInt(), y.toInt()), value.first())
            )
            printPuzzle()
        }
    }

    private fun resetCell(input: String) {
        resetPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            executeAndRevision(
                ResetCellCommand(Coordinates(x.toInt(), y.toInt())),
            )
            printPuzzle()
        }
    }

    private fun analyzePuzzle(input: String) {
        analyzePattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val roundsOrNull = if (value.isNotBlank()) value.trim().toInt() else null
            executeAndRevision(
                AnalyzeCommand(roundsOrNull)
            )
            printPuzzle()
        }
    }

    private fun updateCandidates() {
        executeAndRevision(
            UpdateCandidatesCommand()
        )
        printPuzzle()
    }

    private fun updateStrongLinks() {
        executeAndRevision(
            UpdateStrongLinksCommand()
        )
        printPuzzle()
    }

    private fun eliminateCandidates() {
        executeAndRevision(
            EliminateCandidatesCommand()
        )
        printPuzzle()
    }

    private fun deduceValues() {
        executeAndRevision(
            DeduceValuesCommand()
        )
        printPuzzle()
    }

    private fun toggleCandidate(input: String) {
        toggleCandidatePattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            executeAndRevision(
                ToggleCandidateCommand(Coordinates(x.toInt(), y.toInt()), value.first())
            )
            printPuzzle()
        }
    }

    private fun undo() {
        try {
            PuzzleRevisionService.previousRevision().let { previousRevision ->
                puzzle = previousRevision.puzzle
                printPuzzle()
                PuzzleMessageBroker.message("Switched to revision: ${previousRevision.description}")
            }
        } catch (e: PuzzleRevisionService.PuzzleRevisionException) {
            PuzzleMessageBroker.error("Undo failed: ${e.message}")
        }
    }

    private fun executeAndRevision(command: Command) {
        // TODO how to detect if there were any actual changes?
        CommandExecutorService.executeCommandOnPuzzle(command, puzzle)
        val newRevision = PuzzleRevisionService.newRevision(puzzle)
        PuzzleMessageBroker.message("Stored new revision: $newRevision")
    }
}
