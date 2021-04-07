package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.CommandExecutorService

abstract class SudokuBuilder(
    protected val sudoku: Sudoku,
    protected val messageBroker: SudokuMessageBroker
) {

    fun withGiven(symbol: Symbol, coordinates: Coordinates): SudokuBuilder {
        CommandExecutorService(messageBroker).executeCommandOnSudoku(
            SetCellGivenCommand(coordinates, symbol),
            sudoku
        )
        return this
    }

    fun build(): Sudoku = sudoku
}
