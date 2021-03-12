package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.command.AnalyzeCommand
import fi.thakki.sudokusolver.command.Command
import fi.thakki.sudokusolver.command.DeduceValuesCommand
import fi.thakki.sudokusolver.command.EliminateCandidatesCommand
import fi.thakki.sudokusolver.command.ResetCellCommand
import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.command.SetCellValueCommand
import fi.thakki.sudokusolver.command.UpdateCandidatesCommand
import fi.thakki.sudokusolver.command.UpdateStrongLinksCommand
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer

object CommandExecutorService {

    fun executeCommandOnPuzzle(command: Command, puzzle: Puzzle) =
        when (command) {
            is SetCellGivenCommand ->
                PuzzleMutationService(puzzle).setCellGiven(command.coordinates, command.value)
            is SetCellValueCommand ->
                PuzzleMutationService(puzzle).setCellValue(command.coordinates, command.value)
            is ResetCellCommand ->
                PuzzleMutationService(puzzle).resetCell(command.coordinates)
            is AnalyzeCommand ->
                command.rounds?.let { rounds ->
                    PuzzleAnalyzer(puzzle).analyze(rounds)
                } ?: PuzzleAnalyzer(puzzle).analyze()
            is UpdateCandidatesCommand ->
                PuzzleAnalyzer(puzzle).updateCandidatesOnly()
            is UpdateStrongLinksCommand ->
                PuzzleAnalyzer(puzzle).updateStrongLinksOnly()
            is EliminateCandidatesCommand ->
                PuzzleAnalyzer(puzzle).eliminateCandidatesOnly()
            is DeduceValuesCommand ->
                PuzzleAnalyzer(puzzle).deduceValuesOnly()
            else -> Unit
        }
}
