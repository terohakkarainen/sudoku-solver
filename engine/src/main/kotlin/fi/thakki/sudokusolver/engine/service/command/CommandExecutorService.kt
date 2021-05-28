package fi.thakki.sudokusolver.engine.service.command

import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.service.analyzer.AnalyzeResult
import fi.thakki.sudokusolver.engine.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuMutationService
import fi.thakki.sudokusolver.engine.service.solver.GuessingSolver

class CommandExecutorService(private val messageBroker: SudokuMessageBroker) {

    class UnhandledCommandException(command: Command) : RuntimeException("Don't know how to handle command $command")

    @Suppress("ComplexMethod")
    fun executeCommandOnSudoku(command: Command, sudoku: Sudoku): CommandOutcome =
        when (command) {
            is SetCellGivenCommand -> {
                SudokuMutationService(sudoku).setCellGiven(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.sudokuModified
            }
            is SetCellValueCommand -> {
                SudokuMutationService(sudoku).setCellValue(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.sudokuModified
            }
            is ResetCellCommand -> {
                SudokuMutationService(sudoku).resetCell(command.coordinates) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.sudokuModified
            }
            is AnalyzeCommand ->
                analyzeResultToCommandOutcome(
                    command.rounds?.let { rounds ->
                        SudokuAnalyzer(sudoku, messageBroker).analyze(rounds)
                    } ?: SudokuAnalyzer(sudoku, messageBroker).analyze()
                )
            is UpdateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    SudokuAnalyzer(sudoku, messageBroker).updateCandidatesOnly()
                )
            is UpdateStrongLinksCommand ->
                analyzeResultToCommandOutcome(
                    SudokuAnalyzer(sudoku, messageBroker).updateStrongLinksOnly()
                )
            is EliminateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    SudokuAnalyzer(sudoku, messageBroker).eliminateCandidatesOnly()
                )
            is DeduceValuesCommand ->
                analyzeResultToCommandOutcome(
                    SudokuAnalyzer(sudoku, messageBroker).deduceValuesOnly()
                )
            is ToggleCandidateCommand -> {
                SudokuMutationService(sudoku).toggleCandidate(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.sudokuModified
            }
            is GuessCommand ->
                GuessingSolver(sudoku, messageBroker).solve()
                    ?.let { solvedSudoku ->
                        CommandOutcome(sudokuModified = true, resultingSudoku = solvedSudoku)
                    } ?: CommandOutcome.sudokuNotModified
            else -> throw UnhandledCommandException(command)
        }

    private fun analyzeResultToCommandOutcome(analyzeResult: AnalyzeResult): CommandOutcome =
        when (analyzeResult) {
            AnalyzeResult.NoChanges -> CommandOutcome.sudokuNotModified
            else -> CommandOutcome.sudokuModified
        }
}
