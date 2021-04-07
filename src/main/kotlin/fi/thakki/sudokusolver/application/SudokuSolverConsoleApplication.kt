package fi.thakki.sudokusolver.application

import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.print.SudokuPrinter
import fi.thakki.sudokusolver.service.SudokuConstraintChecker
import fi.thakki.sudokusolver.util.SudokuLoader
import java.io.File

@Suppress("TooManyFunctions")
class SudokuSolverConsoleApplication(sudokuFileName: String) {

    private val messageBroker = ConsoleApplicationMessageBroker
    private var sudoku: Sudoku
    private var exitRequested: Boolean = false

    init {
        File(sudokuFileName).inputStream().use { fileInputStream ->
            sudoku = SudokuLoader.newSudokuFromStream(fileInputStream, messageBroker)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        SudokuSolverActions(sudoku, messageBroker).initialSudokuRevision()
        while (sudokuInProgress() && !exitRequested) {
            try {
                printPrompt()
                translateUserInputToCommand()
            } catch (e: Exception) {
                messageBroker.error("Error: ${e.message}")
            }
        }
    }

    private fun sudokuInProgress() =
        sudoku.state != Sudoku.State.COMPLETE

    private fun printPrompt() {
        messageBroker.message(
            "? > help | R:${sudoku.revision}, ${sudoku.readinessPercentage()}% | Enter command: ",
            putLineFeed = false
        )
    }

    @Suppress("ComplexMethod")
    private fun translateUserInputToCommand() {
        val input = readLine()?.trim()
        fun inputMatches(regex: Regex): Boolean = checkNotNull(input).toLowerCase().matches(regex)
        when {
            input == null -> unknownCommandError()
            inputMatches(quitPattern) -> exitApplication()
            inputMatches(printPattern) -> printSudoku()
            inputMatches(setPattern) -> setCellValue(input)
            inputMatches(resetPattern) -> resetValue(input)
            inputMatches(analyzePattern) -> analyzeSudoku(input)
            inputMatches(highlightPattern) -> printSudokuWithHighlighting(input)
            inputMatches(updateCandidatesPattern) -> updateCandidates()
            inputMatches(updateStrongLinksPattern) -> updateStrongLinks()
            inputMatches(eliminateCandidatesPattern) -> eliminateCandidates()
            inputMatches(deduceValuesPattern) -> deduceValues()
            inputMatches(toggleCandidatePattern) -> toggleCandidate(input)
            inputMatches(undoPattern) -> undo()
            inputMatches(helpPattern) -> help()
            else -> unknownCommandError()
        }
    }

    private fun exitApplication() {
        exitRequested = true
    }

    private fun printSudoku() {
        SudokuPrinter(sudoku).printSudoku()
    }

    private fun setCellValue(input: String) {
        setPattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            SudokuSolverActions(sudoku, messageBroker).setCellValue(Coordinates(x.toInt(), y.toInt()), value.first())
            printSudoku()
        }
    }

    private fun resetValue(input: String) {
        resetPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            SudokuSolverActions(sudoku, messageBroker).resetCell(Coordinates(x.toInt(), y.toInt()))
            printSudoku()
        }
    }

    private fun analyzeSudoku(input: String) {
        analyzePattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val roundsOrNull = if (value.isNotBlank()) value.trim().toInt() else null
            SudokuSolverActions(sudoku, messageBroker).analyzeSudoku(roundsOrNull)
            printSudoku()
        }
    }

    private fun printSudokuWithHighlighting(input: String) {
        highlightPattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val symbol = value.first()
            SudokuConstraintChecker(sudoku).checkSymbolIsSupported(symbol)
            SudokuPrinter(sudoku).printSudoku(highlightedSymbol = symbol)
        }
    }

    private fun updateCandidates() {
        SudokuSolverActions(sudoku, messageBroker).updateCandidates()
        printSudoku()
    }

    private fun updateStrongLinks() {
        SudokuSolverActions(sudoku, messageBroker).updateStrongLinks()
        printSudoku()
    }

    private fun eliminateCandidates() {
        SudokuSolverActions(sudoku, messageBroker).eliminateCandidates()
        printSudoku()
    }

    private fun deduceValues() {
        SudokuSolverActions(sudoku, messageBroker).deduceValues()
        printSudoku()
    }

    private fun toggleCandidate(input: String) {
        toggleCandidatePattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            SudokuSolverActions(sudoku, messageBroker).toggleCandidate(Coordinates(x.toInt(), y.toInt()), value.first())
            printSudoku()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun undo() {
        try {
            sudoku = SudokuSolverActions(sudoku, messageBroker).undo()
            printSudoku()
            messageBroker.message("Switched to revision: ${sudoku.revision}")
        } catch (e: Exception) {
            messageBroker.error("Undo failed: ${e.message}")
        }
    }

    private fun help() {
        messageBroker.message("q > quit")
        messageBroker.message("p > print sudoku")
        messageBroker.message("a n > analyze n rounds")
        messageBroker.message("s x,y symbol > set value to x,y")
        messageBroker.message("r x,y > reset cell value")
        messageBroker.message("h symbol > print sudoku with only symbol candidates shown")
        messageBroker.message("z > undo last change")
        messageBroker.message("u > update sudoku candidates")
        messageBroker.message("l > rebuild strong links in sudoku")
        messageBroker.message("e > try to eliminate candidates in sudoku")
        messageBroker.message("d > try to deduce a value in sudoku")
        messageBroker.message("t x,y symbol > toggle a candidate symbol in cell")
    }

    private fun unknownCommandError() {
        messageBroker.error("unknown command")
    }

    companion object {
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
    }
}
