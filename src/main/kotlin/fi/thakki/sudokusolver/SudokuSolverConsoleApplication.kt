package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleConstraintViolationException
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.util.PuzzleBuilder
import kotlin.system.exitProcess

class SudokuSolverConsoleApplication {

    private val puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_9X9)
        .withGiven("9", Coordinates(1, 8))
        .withGiven("6", Coordinates(6, 8))
        .withGiven("5", Coordinates(1, 7))
        .withGiven("7", Coordinates(2, 7))
        .withGiven("4", Coordinates(5, 7))
        .withGiven("1", Coordinates(0, 6))
        .withGiven("9", Coordinates(3, 6))
        .withGiven("2", Coordinates(4, 6))
        .withGiven("3", Coordinates(5, 6))
        .withGiven("8", Coordinates(6, 6))
        .withGiven("1", Coordinates(1, 5))
        .withGiven("8", Coordinates(2, 5))
        .withGiven("4", Coordinates(4, 5))
        .withGiven("3", Coordinates(8, 5))
        .withGiven("7", Coordinates(3, 4))
        .withGiven("2", Coordinates(5, 4))
        .withGiven("6", Coordinates(0, 3))
        .withGiven("1", Coordinates(4, 3))
        .withGiven("4", Coordinates(6, 3))
        .withGiven("8", Coordinates(7, 3))
        .withGiven("9", Coordinates(2, 2))
        .withGiven("5", Coordinates(3, 2))
        .withGiven("7", Coordinates(4, 2))
        .withGiven("6", Coordinates(5, 2))
        .withGiven("1", Coordinates(8, 2))
        .withGiven("4", Coordinates(3, 1))
        .withGiven("5", Coordinates(6, 1))
        .withGiven("6", Coordinates(7, 1))
        .withGiven("1", Coordinates(2, 0))
        .withGiven("9", Coordinates(7, 0))
        .build()

    //    private val puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_4X4)
//        .withGiven("3", Coordinates(0, 1))
//        .withGiven("2", Coordinates(1, 0))
//        .withGiven("2", Coordinates(2, 1))
//        .withGiven("3", Coordinates(3, 0))
//        .withGiven("4", Coordinates(0, 2))
//        .withGiven("1", Coordinates(1, 3))
//        .withGiven("1", Coordinates(2, 2))
//        .withGiven("4", Coordinates(3, 3))
//        .build()
    private val setPattern = Regex("^s (.*),(.*) (.*)$")
    private val resetPattern = Regex("^r (.*),(.*)$")
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
                SetCellValueCommand(Coordinates(x.toInt(), y.toInt()), value),
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
