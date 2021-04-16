package fi.thakki.sudokusolver.service.builder

import fi.thakki.sudokusolver.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbols

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
