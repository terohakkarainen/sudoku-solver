package fi.thakki.sudokusolver.engine.service.builder

import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.command.CommandExecutorService
import fi.thakki.sudokusolver.engine.service.command.SetCellGivenCommand
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker

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
