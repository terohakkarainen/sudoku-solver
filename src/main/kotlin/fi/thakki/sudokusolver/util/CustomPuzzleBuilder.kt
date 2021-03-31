package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbols

class CustomPuzzleBuilder(
    puzzleFile: PuzzleFile,
    messageBroker: PuzzleMessageBroker
) : PuzzleBuilder(
    puzzle = Puzzle(
        dimension = puzzleFile.dimension,
        symbols = Symbols(puzzleFile.symbols),
        cells = Puzzle.cellsForDimension(puzzleFile.dimension, Symbols(puzzleFile.symbols)),
        coordinatesForRegions = puzzleFile.getCoordinatesForRegions()
    ),
    messageBroker = messageBroker
)
