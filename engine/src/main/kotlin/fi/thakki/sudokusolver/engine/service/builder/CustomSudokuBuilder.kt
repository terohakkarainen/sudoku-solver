package fi.thakki.sudokusolver.engine.service.builder

import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbols
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker

class CustomSudokuBuilder(
    sudokuFile: SudokuFile,
    messageBroker: SudokuMessageBroker
) : SudokuBuilder(
    sudoku = Sudoku(
        dimension = sudokuFile.dimension,
        symbols = Symbols(sudokuFile.symbols),
        cells = Sudoku.cellsForDimension(sudokuFile.dimension, Symbols(sudokuFile.symbols)),
        coordinatesForRegions = sudokuFile.getCoordinatesForRegions()
    ),
    messageBroker = messageBroker
)
