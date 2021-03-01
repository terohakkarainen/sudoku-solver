package fi.thakki.sudokusolver.command

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Symbol

interface Command

data class SetCellGivenCommand(val coordinates: Coordinates, val value: Symbol) : Command

data class SetCellValueCommand(val coordinates: Coordinates, val value: Symbol) : Command

data class ResetCellCommand(val coordinates: Coordinates) : Command