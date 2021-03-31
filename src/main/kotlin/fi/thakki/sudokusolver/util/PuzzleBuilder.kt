package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.CommandExecutorService

abstract class PuzzleBuilder(
    protected val puzzle: Puzzle,
    protected val messageBroker: PuzzleMessageBroker
) {

    fun withGiven(symbol: Symbol, coordinates: Coordinates): PuzzleBuilder {
        CommandExecutorService(messageBroker).executeCommandOnPuzzle(
            SetCellGivenCommand(coordinates, symbol),
            puzzle
        )
        return this
    }

    fun build(): Puzzle =
        puzzle
}
