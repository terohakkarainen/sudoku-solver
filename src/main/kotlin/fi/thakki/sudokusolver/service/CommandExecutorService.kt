package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.CommandOutcome
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.GuessCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.service.analyzer.AnalyzeResult
import fi.thakki.sudokusolver.service.solver.GuessingSolver
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer

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
