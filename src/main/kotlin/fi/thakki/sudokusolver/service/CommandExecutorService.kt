package fi.thakki.sudokusolver.service

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

object CommandExecutorService {

    class UnhandledCommandException(command: Command) : RuntimeException("Don't know how to handle command $command")

    fun executeCommandOnPuzzle(command: Command, puzzle: Puzzle): CommandOutcome =
        when (command) {
            is SetCellGivenCommand -> {
                PuzzleMutationService(puzzle).setCellGiven(command.coordinates, command.value)
                CommandOutcome.puzzleModified
            }
            is SetCellValueCommand -> {
                PuzzleMutationService(puzzle).setCellValue(command.coordinates, command.value)
                CommandOutcome.puzzleModified
            }
            is ResetCellCommand -> {
                PuzzleMutationService(puzzle).resetCell(command.coordinates)
                CommandOutcome.puzzleModified
            }
            is AnalyzeCommand ->
                analyzeResultToCommandOutcome(
                    command.rounds?.let { rounds ->
                        PuzzleAnalyzer(puzzle).analyze(rounds)
                    } ?: PuzzleAnalyzer(puzzle).analyze()
                )
            is UpdateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle).updateCandidatesOnly()
                )
            is UpdateStrongLinksCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle).updateStrongLinksOnly()
                )
            is EliminateCandidatesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle).eliminateCandidatesOnly()
                )
            is DeduceValuesCommand ->
                analyzeResultToCommandOutcome(
                    PuzzleAnalyzer(puzzle).deduceValuesOnly()
                )
            is ToggleCandidateCommand -> {
                PuzzleMutationService(puzzle).toggleCandidate(command.coordinates, command.value)
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
