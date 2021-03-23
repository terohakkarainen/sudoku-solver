package fi.thakki.sudokusolver

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
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleRevisionService
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer

@Suppress("TooManyFunctions")
class PuzzleActions(private val puzzle: Puzzle) {

    private val puzzleAnalyzer = PuzzleAnalyzer(puzzle)

    fun initialPuzzleRevision() {
        PuzzleRevisionService.newRevision(puzzle).also { newRevision ->
            puzzle.revision = newRevision
            PuzzleMessageBroker.message("Puzzle initialized, starting game with revision $newRevision")
        }
    }

    fun setCellValue(coordinates: Coordinates, value: Symbol) {
        revisionAfter {
            execute(SetCellValueCommand(coordinates, value)).also {
                puzzleAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun resetCell(coordinates: Coordinates) {
        revisionAfter {
            execute(ResetCellCommand(coordinates)).also {
                puzzleAnalyzer.updateCandidatesOnly()
            }
        }
    }

    fun analyzePuzzle(rounds: Int?) {
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

    fun undo(): Puzzle =
        PuzzleRevisionService.previousRevision().let { puzzleRevision ->
            val newPuzzle = puzzleRevision.puzzle.apply {
                revision = puzzleRevision.description
            }
            execute(UpdateStrongLinksCommand(), newPuzzle)
            newPuzzle
        }

    private fun execute(command: Command, targetPuzzle: Puzzle = puzzle): CommandOutcome =
        CommandExecutorService.executeCommandOnPuzzle(command, targetPuzzle)

    private fun revisionAfter(runner: () -> CommandOutcome) {
        runner().let { outcome ->
            if (outcome.puzzleModified) {
                PuzzleRevisionService.newRevision(puzzle).let { newRevision ->
                    puzzle.revision = newRevision
                    PuzzleMessageBroker.message("Stored new revision: $newRevision")
                }
            }
        }
    }
}
