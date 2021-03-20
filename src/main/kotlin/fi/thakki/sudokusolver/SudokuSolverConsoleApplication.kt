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
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer
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
    private val helpPattern = Regex("^\\?$")

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        PuzzleRevisionService.newRevision(puzzle).also { newRevision ->
            puzzle.revision = newRevision
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
            helpPattern.matches(input) -> help()
            else -> unknownCommandError()
        }
    }

    private fun printPrompt() {
        PuzzleMessageBroker.message(
            "? > help | R:${puzzle.revision}, ${puzzle.readinessPercentage()}% | Enter command: ",
            putLineFeed = false
        )
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
                puzzle.revision = previousRevision.description
                PuzzleAnalyzer(puzzle).updateStrongLinksOnly()
                printPuzzle()
                PuzzleMessageBroker.message("Switched to revision: ${previousRevision.description}")
            }
        } catch (e: PuzzleRevisionService.PuzzleRevisionException) {
            PuzzleMessageBroker.error("Undo failed: ${e.message}")
        }
    }

    private fun help() {
        PuzzleMessageBroker.message("q > quit")
        PuzzleMessageBroker.message("p > print puzzle")
        PuzzleMessageBroker.message("a n > analyze n rounds")
        PuzzleMessageBroker.message("s x,y symbol > set value to x,y")
        PuzzleMessageBroker.message("r x,y > reset cell value")
        PuzzleMessageBroker.message("h symbol > print puzzle with only symbol candidates shown")
        PuzzleMessageBroker.message("z > undo last change")
        PuzzleMessageBroker.message("u > update puzzle candidates")
        PuzzleMessageBroker.message("l > rebuild strong links in puzzle")
        PuzzleMessageBroker.message("e > try to eliminate candidates in puzzle")
        PuzzleMessageBroker.message("d > try to deduce a value in puzzle")
        PuzzleMessageBroker.message("t x,y symbol > toggle a candidate symbol in cell")
    }

    private fun executeAndRevision(command: Command) {
        CommandExecutorService.executeCommandOnPuzzle(command, puzzle).let { outcome ->
            if (outcome.puzzleModified) {
                PuzzleRevisionService.newRevision(puzzle).let { newRevision ->
                    puzzle.revision = newRevision
                    PuzzleMessageBroker.message("Stored new revision: $newRevision")
                }
            }
        }
    }
}
