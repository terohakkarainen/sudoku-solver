package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.PuzzleConstraintChecker
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlin.system.exitProcess

@Suppress("TooManyFunctions")
class SudokuSolverConsoleApplication(puzzleFileName: String) {

    private val messageBroker = ConsoleApplicationMessageBroker
    private var puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFileName, messageBroker)

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        PuzzleActions(puzzle, messageBroker).initialPuzzleRevision()
        while (true) {
            try {
                exitIfComplete()
                printPrompt()
                translateUserInputToCommand()
            } catch (e: Exception) {
                messageBroker.error("Error: ${e.message}")
            }
        }
    }

    private fun exitIfComplete() {
        if (puzzle.isComplete()) {
            messageBroker.message("Puzzle complete!")
            exitProcess(0)
        }
    }

    private fun printPrompt() {
        messageBroker.message(
            "? > help | R:${puzzle.revision}, ${puzzle.readinessPercentage()}% | Enter command: ",
            putLineFeed = false
        )
    }

    @Suppress("ComplexMethod")
    private fun translateUserInputToCommand() {
        val input = readLine()?.trim()?.toLowerCase()
        when {
            input == null -> unknownCommandError()
            quitPattern.matches(input) -> exitApplication()
            printPattern.matches(input) -> printPuzzle()
            setPattern.matches(input) -> setCellValue(input)
            resetPattern.matches(input) -> resetValue(input)
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

    private fun exitApplication() {
        exitProcess(0)
    }

    private fun printPuzzle() {
        SudokuPrinter(puzzle).printPuzzle()
    }

    private fun setCellValue(input: String) {
        setPattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            PuzzleActions(puzzle, messageBroker).setCellValue(Coordinates(x.toInt(), y.toInt()), value.first())
            printPuzzle()
        }
    }

    private fun resetValue(input: String) {
        resetPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            PuzzleActions(puzzle, messageBroker).resetCell(Coordinates(x.toInt(), y.toInt()))
            printPuzzle()
        }
    }

    private fun analyzePuzzle(input: String) {
        analyzePattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val roundsOrNull = if (value.isNotBlank()) value.trim().toInt() else null
            PuzzleActions(puzzle, messageBroker).analyzePuzzle(roundsOrNull)
            printPuzzle()
        }
    }

    private fun printPuzzleWithHighlighting(input: String) {
        highlightPattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val symbol = value.first()
            PuzzleConstraintChecker(puzzle).checkSymbolIsSupported(symbol)
            SudokuPrinter(puzzle).printPuzzle(highlightedSymbol = symbol)
        }
    }

    private fun updateCandidates() {
        PuzzleActions(puzzle, messageBroker).updateCandidates()
        printPuzzle()
    }

    private fun updateStrongLinks() {
        PuzzleActions(puzzle, messageBroker).updateStrongLinks()
        printPuzzle()
    }

    private fun eliminateCandidates() {
        PuzzleActions(puzzle, messageBroker).eliminateCandidates()
        printPuzzle()
    }

    private fun deduceValues() {
        PuzzleActions(puzzle, messageBroker).deduceValues()
        printPuzzle()
    }

    private fun toggleCandidate(input: String) {
        toggleCandidatePattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            PuzzleActions(puzzle, messageBroker).toggleCandidate(Coordinates(x.toInt(), y.toInt()), value.first())
            printPuzzle()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun undo() {
        try {
            puzzle = PuzzleActions(puzzle, messageBroker).undo()
            printPuzzle()
            messageBroker.message("Switched to revision: ${puzzle.revision}")
        } catch (e: Exception) {
            messageBroker.error("Undo failed: ${e.message}")
        }
    }

    private fun help() {
        messageBroker.message("q > quit")
        messageBroker.message("p > print puzzle")
        messageBroker.message("a n > analyze n rounds")
        messageBroker.message("s x,y symbol > set value to x,y")
        messageBroker.message("r x,y > reset cell value")
        messageBroker.message("h symbol > print puzzle with only symbol candidates shown")
        messageBroker.message("z > undo last change")
        messageBroker.message("u > update puzzle candidates")
        messageBroker.message("l > rebuild strong links in puzzle")
        messageBroker.message("e > try to eliminate candidates in puzzle")
        messageBroker.message("d > try to deduce a value in puzzle")
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
