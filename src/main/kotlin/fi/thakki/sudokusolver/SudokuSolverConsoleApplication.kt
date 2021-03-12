package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleConstraintChecker
import fi.thakki.sudokusolver.service.PuzzleConstraintViolationException
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlin.system.exitProcess

class SudokuSolverConsoleApplication(puzzleFileName: String) {

    private val puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFileName)
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

    fun eventLoop() {
        PuzzleMessageBroker.message("Puzzle initialized, starting game.")
        while (true) {
            try {
                exitIfComplete()
                getUserInput()
            } catch (e: PuzzleConstraintViolationException) {
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

    private fun getUserInput() {
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
            CommandExecutorService.executeCommandOnPuzzle(
                SetCellValueCommand(Coordinates(x.toInt(), y.toInt()), value.first()),
                puzzle
            )
            printPuzzle()
        }
    }

    private fun resetCell(input: String) {
        resetPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            CommandExecutorService.executeCommandOnPuzzle(
                ResetCellCommand(Coordinates(x.toInt(), y.toInt())),
                puzzle
            )
            printPuzzle()
        }
    }

    private fun analyzePuzzle(input: String) {
        analyzePattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val roundsOrNull = if (value.isNotBlank()) value.trim().toInt() else null
            CommandExecutorService.executeCommandOnPuzzle(AnalyzeCommand(roundsOrNull), puzzle)
            printPuzzle()
        }
    }

    private fun updateCandidates() {
        CommandExecutorService.executeCommandOnPuzzle(UpdateCandidatesCommand(), puzzle)
        printPuzzle()
    }

    private fun updateStrongLinks() {
        CommandExecutorService.executeCommandOnPuzzle(UpdateStrongLinksCommand(), puzzle)
        printPuzzle()
    }

    private fun eliminateCandidates() {
        CommandExecutorService.executeCommandOnPuzzle(EliminateCandidatesCommand(), puzzle)
        printPuzzle()
    }

    private fun deduceValues() {
        CommandExecutorService.executeCommandOnPuzzle(DeduceValuesCommand(), puzzle)
        printPuzzle()
    }
}
