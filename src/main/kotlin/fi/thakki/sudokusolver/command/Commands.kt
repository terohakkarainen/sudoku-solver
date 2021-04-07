package fi.thakki.sudokusolver.command

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Symbol

interface Command

data class SetCellGivenCommand(val coordinates: Coordinates, val value: Symbol) : Command

data class SetCellValueCommand(val coordinates: Coordinates, val value: Symbol) : Command

data class ResetCellCommand(val coordinates: Coordinates) : Command

data class AnalyzeCommand(val rounds: Int?) : Command

class UpdateCandidatesCommand : Command

class UpdateStrongLinksCommand : Command

class EliminateCandidatesCommand : Command

class DeduceValuesCommand : Command

data class ToggleCandidateCommand(val coordinates: Coordinates, val value: Symbol) : Command

data class CommandOutcome(val sudokuModified: Boolean) {
    companion object {
        val sudokuNotModified = CommandOutcome(false)
        val sudokuModified = CommandOutcome(true)
    }
}
