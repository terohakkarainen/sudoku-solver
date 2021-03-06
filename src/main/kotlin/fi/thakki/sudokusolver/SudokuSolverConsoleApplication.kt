package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleConstraintViolationException
import fi.thakki.sudokusolver.util.PuzzleBuilder
import kotlin.system.exitProcess

class SudokuSolverConsoleApplication {

    private val puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_4X4)
        .withGiven("3", Coordinates(0, 1))
        .withGiven("2", Coordinates(1, 0))
        .withGiven("2", Coordinates(2, 1))
        .withGiven("3", Coordinates(3, 0))
        .withGiven("4", Coordinates(0, 2))
        .withGiven("1", Coordinates(1, 3))
        .withGiven("1", Coordinates(2, 2))
        .withGiven("4", Coordinates(3, 3))
        .build()
    private val setPattern = Regex("^s (.*),(.*) (.*)$")
    private val resetPattern = Regex("^r (.*),(.*)$")
    private val analyzePattern = Regex("^a$")

    fun eventLoop() {
        while (true) {
            try {
                printPrompt()
                val input = readLine()?.trim()?.toLowerCase()
                when {
                    input == null -> println("unknown command")
                    input == "q" -> exitProcess(0)
                    input == "p" -> printPuzzle()
                    setPattern.matches(input) -> setCellValue(input)
                    resetPattern.matches(input) -> resetCell(input)
                    analyzePattern.matches(input) -> analyzePuzzle()
                    else -> println("unknown command")
                }
            } catch (e: PuzzleConstraintViolationException) {
                println("Error: ${e.message}")
            }
        }
    }

    private fun printPrompt() {
        println("SudokuSolver | q > quit | p > print | a > analyze | s_x,y_v > set | r_x,y > reset")
        println("---------------------------------------------------------------------------------")
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
