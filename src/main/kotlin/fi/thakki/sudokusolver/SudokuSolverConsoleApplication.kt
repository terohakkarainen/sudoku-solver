package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.service.PuzzleConstraintChecker
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlin.system.exitProcess

@Suppress("TooManyFunctions")
class SudokuSolverConsoleApplication(puzzleFileName: String) {

    private var puzzle = PuzzleLoader.newPuzzleFromFile(puzzleFileName)

    @Suppress("TooGenericExceptionCaught")
    fun eventLoop() {
        PuzzleActions(puzzle).initialPuzzleRevision()
        while (true) {
            try {
                exitIfComplete()
                printPrompt()
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

    private fun printPrompt() {
        PuzzleMessageBroker.message(
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
            PuzzleActions(puzzle).setCellValue(Coordinates(x.toInt(), y.toInt()), value.first())
            printPuzzle()
        }
    }

    private fun resetValue(input: String) {
        resetPattern.find(input)?.let { matchResult ->
            val (x, y) = matchResult.destructured
            PuzzleActions(puzzle).resetCell(Coordinates(x.toInt(), y.toInt()))
            printPuzzle()
        }
    }

    private fun analyzePuzzle(input: String) {
        analyzePattern.find(input)?.let { matchResult ->
            val (value) = matchResult.destructured
            val roundsOrNull = if (value.isNotBlank()) value.trim().toInt() else null
            PuzzleActions(puzzle).analyzePuzzle(roundsOrNull)
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
        PuzzleActions(puzzle).updateCandidates()
        printPuzzle()
    }

    private fun updateStrongLinks() {
        PuzzleActions(puzzle).updateStrongLinks()
        printPuzzle()
    }

    private fun eliminateCandidates() {
        PuzzleActions(puzzle).eliminateCandidates()
        printPuzzle()
    }

    private fun deduceValues() {
        PuzzleActions(puzzle).deduceValues()
        printPuzzle()
    }

    private fun toggleCandidate(input: String) {
        toggleCandidatePattern.find(input)?.let { matchResult ->
            val (x, y, value) = matchResult.destructured
            PuzzleActions(puzzle).toggleCandidate(Coordinates(x.toInt(), y.toInt()), value.first())
            printPuzzle()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun undo() {
        try {
            puzzle = PuzzleActions(puzzle).undo()
            printPuzzle()
            PuzzleMessageBroker.message("Switched to revision: ${puzzle.revision}")
        } catch (e: Exception) {
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

    private fun unknownCommandError() {
        PuzzleMessageBroker.error("unknown command")
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
