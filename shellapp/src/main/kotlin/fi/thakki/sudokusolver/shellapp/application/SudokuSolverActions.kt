package fi.thakki.sudokusolver.shellapp.application

import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.engine.service.command.AnalyzeCommand
import fi.thakki.sudokusolver.engine.service.command.Command
import fi.thakki.sudokusolver.engine.service.command.CommandExecutorService
import fi.thakki.sudokusolver.engine.service.command.CommandOutcome
import fi.thakki.sudokusolver.engine.service.command.DeduceValuesCommand
import fi.thakki.sudokusolver.engine.service.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.engine.service.command.GuessCommand
import fi.thakki.sudokusolver.engine.service.command.ResetCellCommand
import fi.thakki.sudokusolver.engine.service.command.SetCellValueCommand
import fi.thakki.sudokusolver.engine.service.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.engine.service.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.engine.service.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.engine.service.mutation.SudokuRevisionService
import fi.thakki.sudokusolver.engine.service.mutation.SudokuSerializationService

@Suppress("TooManyFunctions")
class SudokuSolverActions(
    private val sudoku: Sudoku,
    private val revisionService: SudokuRevisionService,
    private val messageBroker: SudokuMessageBroker
) {

    private val sudokuAnalyzer = SudokuAnalyzer(sudoku, messageBroker)

    fun initialSudokuRevision() {
        revisionService.newRevision(sudoku, "Initial revision").also { revisionInfo ->
            sudoku.revisionInformation = revisionInfo
        }
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        revisionAfter("Cell $coordinates set to $value") {
            execute(SetCellValueCommand(coordinates, value)).also {
                sudokuAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun resetCell(coordinates: Coordinates) {
        revisionAfter("Cell $coordinates reset") {
            execute(ResetCellCommand(coordinates)).also {
                sudokuAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun analyzeSudoku(rounds: Int?) {
        revisionAfter("Analyze ${rounds ?: 1} rounds") {
            execute(AnalyzeCommand(rounds))
        }
    }

    fun updateCandidates() {
        revisionAfter("Update candidates") {
            execute(UpdateCandidatesCommand())
        }
    }

    fun updateStrongLinks() {
        revisionAfter("Update strong links") {
            execute(UpdateStrongLinksCommand())
        }
    }

    fun eliminateCandidates() {
        revisionAfter("Eliminate candidates") {
            execute(EliminateCandidatesCommand())
        }
    }

    fun deduceValues() {
        revisionAfter("Deduce values") {
            execute(DeduceValuesCommand())
        }
    }

    fun toggleCandidate(coordinates: Coordinates, value: Symbol) {
        revisionAfter("Toggle candidate $value in cell $coordinates") {
            execute(ToggleCandidateCommand(coordinates, value))
        }
    }

    fun undo(): Sudoku =
        revisionService.restorePreviousRevision().let { restoredSudokuRevision ->
            val restoredSudoku = restoredSudokuRevision.sudoku
            if (restoredSudoku.state != Sudoku.State.NOT_ANALYZED_YET) {
                execute(UpdateStrongLinksCommand(), restoredSudoku)
            }
            restoredSudoku
        }

    fun guessSolution(): Sudoku? =
        execute(GuessCommand(), SudokuSerializationService.copyOf(sudoku)).let { outcome ->
            if (outcome.sudokuModified) outcome.resultingSudoku else null
        }

    private fun execute(command: Command, targetSudoku: Sudoku = sudoku): CommandOutcome =
        CommandExecutorService(messageBroker).executeCommandOnSudoku(command, targetSudoku)

    private fun revisionAfter(description: String, runner: () -> CommandOutcome) {
        runner().let { outcome ->
            if (outcome.sudokuModified) {
                revisionService.newRevision(sudoku, description).let { revisionInfo ->
                    sudoku.revisionInformation = revisionInfo
                    messageBroker.message(
                        RevisionMessages.newRevisionStored(revisionInfo)
                    )
                }
            }
        }
    }
}
