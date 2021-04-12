package fi.thakki.sudokusolver.application

import fi.thakki.sudokusolver.BuildConfig
import fi.thakki.sudokusolver.canvas.Color
import fi.thakki.sudokusolver.canvas.ColoredString
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.print.SudokuPrinter
import fi.thakki.sudokusolver.service.SudokuConstraintChecker
import fi.thakki.sudokusolver.util.DateConversions
import fi.thakki.sudokusolver.util.SudokuLoader
import java.io.File

@Suppress("TooManyFunctions")
class SudokuSolverConsoleApplication(pathToSudokuFile: String) {

    private val messageBroker = ConsoleApplicationMessageBroker
    private var sudoku: Sudoku
    private var exitRequested: Boolean = false

    init {
        File(pathToSudokuFile).inputStream().use { fileInputStream ->
            sudoku = SudokuLoader.newSudokuFromStream(fileInputStream, messageBroker)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        SudokuSolverActions(sudoku, messageBroker).initialSudokuRevision()
        messageBroker.message("SudokuSolver version ${BuildConfig.version} started")

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
        fun inColors(fgColor: Color, bgColor: Color, s: String): String =
            ColoredString.of(" $s ", listOf(fgColor.fgCode, bgColor.bgCode))

        val revisionNumber = checkNotNull(sudoku.revisionInformation).number
        messageBroker.message(
            inColors(Color.BLACK, Color.LIGHT_CYAN, "? > help") +
                    inColors(Color.WHITE, Color.BLUE, "R:$revisionNumber, ${sudoku.readinessPercentage()}%") +
                    inColors(Color.BLACK, Color.LIGHT_GRAY, "Enter command >") + " ",
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
            inputMatches(revisionPattern) -> showRevisionInformation()
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
            sudoku.revisionInformation?.let { revisionInfo ->
                messageBroker.message(
                    RevisionMessages.switchedToRevision(revisionInfo)
                )
            }
        } catch (e: Exception) {
            messageBroker.error("Undo failed: ${e.message}")
        }
    }

    private fun help() {
        messageBroker.message("q            | quits the application")
        messageBroker.message("p            | prints current sudoku to screen")
        messageBroker.message("a n          | analyzes the sudoku for n rounds [revisioning]")
        messageBroker.message("s x,y symbol | sets symbol to value of cell (x,y) [revisioning]")
        messageBroker.message("r x,y        | resets value of cell (x,y) [revisioning]")
        messageBroker.message("h symbol     | prints current sudoku with only given symbol candidates shown")
        messageBroker.message("z            | undoes last change and returns to previous revision [revisioning]")
        messageBroker.message("u            | updates sudoku candidates based on current cell values [revisioning]")
        messageBroker.message("l            | rebuilds strong links in current sudoku")
        messageBroker.message("e            | eliminates candidates in current sudoku [revisioning]")
        messageBroker.message("d            | deduces a value in current sudoku [revisioning]")
        messageBroker.message("t x,y symbol | toggles a candidate symbol in cell (x,y) [revisioning]")
        messageBroker.message("r            | shows current revision information")
    }

    private fun showRevisionInformation() {
        sudoku.revisionInformation?.let { revisionInfo ->
            messageBroker.message(
                "currently in revision ${revisionInfo.number}, " +
                        "created at ${DateConversions.toPrintable(revisionInfo.createdAt)}, " +
                        "description: ${revisionInfo.description}"
            )
        }
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
        private val revisionPattern = Regex("^r$")
    }
}
