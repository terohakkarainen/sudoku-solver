package fi.thakki.sudokusolver.application

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.CommandOutcome
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.SudokuRevisionService
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer

@Suppress("TooManyFunctions")
class SudokuSolverActions(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private val sudokuAnalyzer = SudokuAnalyzer(sudoku, messageBroker)

    fun initialSudokuRevision() {
        SudokuRevisionService.newRevision(sudoku).also { newRevision ->
            sudoku.revision = newRevision
            messageBroker.message("Sudoku initialized, starting game with revision $newRevision")
        }
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        revisionAfter {
            execute(SetCellValueCommand(coordinates, value)).also {
                sudokuAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun resetCell(coordinates: Coordinates) {
        revisionAfter {
            execute(ResetCellCommand(coordinates)).also {
                sudokuAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun analyzeSudoku(rounds: Int?) {
        revisionAfter {
            execute(AnalyzeCommand(rounds))
        }
    }

    fun updateCandidates() {
        revisionAfter {
            execute(UpdateCandidatesCommand())
        }
    }

    fun updateStrongLinks() {
        revisionAfter {
            execute(UpdateStrongLinksCommand())
        }
    }

    fun eliminateCandidates() {
        revisionAfter {
            execute(EliminateCandidatesCommand())
        }
    }

    fun deduceValues() {
        revisionAfter {
            execute(DeduceValuesCommand())
        }
    }

    fun toggleCandidate(coordinates: Coordinates, value: Symbol) {
        revisionAfter {
            execute(ToggleCandidateCommand(coordinates, value))
        }
    }

    fun undo(): Sudoku =
        SudokuRevisionService.previousRevision().let { sudokuRevision ->
            val newSudoku = sudokuRevision.sudoku.apply {
                revision = sudokuRevision.description
            }
            if (newSudoku.state != Sudoku.State.NOT_ANALYZED_YET) {
                execute(UpdateStrongLinksCommand(), newSudoku)
            }
            newSudoku
        }

    private fun execute(command: Command, targetSudoku: Sudoku = sudoku): CommandOutcome =
        CommandExecutorService(messageBroker).executeCommandOnSudoku(command, targetSudoku)

    private fun revisionAfter(runner: () -> CommandOutcome) {
        runner().let { outcome ->
            if (outcome.sudokuModified) {
                SudokuRevisionService.newRevision(sudoku).let { newRevision ->
                    sudoku.revision = newRevision
                    messageBroker.message("Stored new revision: $newRevision")
                }
            }
        }
    }
}
