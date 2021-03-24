package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.PuzzleMessageBroker
import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.CommandOutcome
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.ToggleCandidateCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.service.analyzer.AnalyzeResult
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer

class CommandExecutorService(private val messageBroker: PuzzleMessageBroker) {

    class UnhandledCommandException(command: Command) : RuntimeException("Don't know how to handle command $command")

    fun executeCommandOnPuzzle(command: Command, puzzle: Puzzle): CommandOutcome =
        when (command) {
            is SetCellGivenCommand -> {
                PuzzleMutationService(puzzle).setCellGiven(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.puzzleModified
            }
            is SetCellValueCommand -> {
                PuzzleMutationService(puzzle).setCellValue(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.puzzleModified
            }
            is ResetCellCommand -> {
                PuzzleMutationService(puzzle).resetCell(command.coordinates) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.puzzleModified
            }
            is AnalyzeCommand ->
                analyzeResultToCommandOutcome(
                    command.rounds?.let { rounds ->
                        PuzzleAnalyzer(puzzle, messageBroker).analyze(rounds)
                    } ?: PuzzleAnalyzer(puzzle, messageBroker).analyze()
                )
            is UpdateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle, messageBroker).updateCandidatesOnly()
                )
            is UpdateStrongLinksCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle, messageBroker).updateStrongLinksOnly()
                )
            is EliminateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle, messageBroker).eliminateCandidatesOnly()
                )
            is DeduceValuesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle, messageBroker).deduceValuesOnly()
                )
            is ToggleCandidateCommand -> {
                PuzzleMutationService(puzzle).toggleCandidate(command.coordinates, command.value) { message ->
                    messageBroker.message(message)
                }
                CommandOutcome.puzzleModified
            }
            else -> throw UnhandledCommandException(command)
        }

    private fun analyzeResultToCommandOutcome(analyzeResult: AnalyzeResult): CommandOutcome =
        when (analyzeResult) {
            AnalyzeResult.NoChanges -> CommandOutcome.puzzleNotModified
            else -> CommandOutcome.puzzleModified
        }
}
