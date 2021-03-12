package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleConstraintViolationException
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlin.system.exitProcess

class SudokuSolverConsoleApplication(puzzleFileName: String) {

    private val puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFileName)
    private val setPattern = Regex("^s ([0-9]*),([0-9]*) (.)$")
    private val resetPattern = Regex("^r ([0-9]*),([0-9]*)$")
    private val analyzePattern = Regex("^a$")

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
            input == null -> PuzzleMessageBroker.error("unknown command")
            input == "q" -> exitProcess(0)
            input == "p" -> printPuzzle()
            setPattern.matches(input) -> setCellValue(input)
            resetPattern.matches(input) -> resetCell(input)
            analyzePattern.matches(input) -> analyzePuzzle()
            else -> PuzzleMessageBroker.error("unknown command")
        }
    }

    private fun printPrompt() {
        PuzzleMessageBroker.message("SudokuSolver | q > quit | p > print | a > analyze | s_x,y_v > set | r_x,y > reset")
        PuzzleMessageBroker.message("---------------------------------------------------------------------------------")
        PuzzleMessageBroker.message("Enter command: ", putLineFeed = false)
    }

    private fun printPuzzle() {
        SudokuPrinter(puzzle).printPuzzle()
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

    private fun analyzePuzzle() {
        CommandExecutorService.executeCommandOnPuzzle(AnalyzeCommand(), puzzle)
        printPuzzle()
    }
}
