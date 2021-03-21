package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleRevisionService

class PuzzleActions(private val puzzle: Puzzle) {

    fun initialPuzzleRevision() {
        PuzzleRevisionService.newRevision(puzzle).also { newRevision ->
            puzzle.revision = newRevision
            PuzzleMessageBroker.message("Puzzle initialized, starting game with revision $newRevision")
        }
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        executeAndRevision(
            SetCellValueCommand(coordinates, value)
        )
    }

    fun resetCell(coordinates: Coordinates) {
        executeAndRevision(
            ResetCellCommand(coordinates)
        )
    }

    fun analyzePuzzle(rounds: Int?) {
        executeAndRevision(
            AnalyzeCommand(rounds)
        )
    }

    fun updateCandidates() {
        executeAndRevision(
            UpdateCandidatesCommand()
        )
    }

    fun updateStrongLinks() {
        executeAndRevision(
            UpdateStrongLinksCommand()
        )
    }

    fun eliminateCandidates() {
        executeAndRevision(
            EliminateCandidatesCommand()
        )
    }

    fun deduceValues() {
        executeAndRevision(
            DeduceValuesCommand()
        )
    }

    fun toggleCandidate(coordinates: Coordinates, value: Symbol) {
        executeAndRevision(
            ToggleCandidateCommand(coordinates, value)
        )
    }

    fun undo(): Puzzle =
        PuzzleRevisionService.previousRevision().let { puzzleRevision ->
            val newPuzzle = puzzleRevision.puzzle.apply {
                revision = puzzleRevision.description
            }
            CommandExecutorService.executeCommandOnPuzzle(UpdateStrongLinksCommand(), newPuzzle)
            newPuzzle
        }

    private fun executeAndRevision(command: Command) {
        CommandExecutorService.executeCommandOnPuzzle(command, puzzle).let { outcome ->
            if (outcome.puzzleModified) {
                PuzzleRevisionService.newRevision(puzzle).let { newRevision ->
                    puzzle.revision = newRevision
                    PuzzleMessageBroker.message("Stored new revision: $newRevision")
                }
            }
        }
    }
}
