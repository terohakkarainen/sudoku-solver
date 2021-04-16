package fi.thakki.sudokusolver.service.builder

import fi.thakki.sudokusolver.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.RegionFunc
import fi.thakki.sudokusolver.model.Symbols
import fi.thakki.sudokusolver.service.builder.StandardDimensions.DIMENSION_4
import fi.thakki.sudokusolver.service.builder.StandardDimensions.DIMENSION_9
import fi.thakki.sudokusolver.service.builder.StandardSymbols.SYMBOLS_14
import fi.thakki.sudokusolver.service.builder.StandardSymbols.SYMBOLS_19

class StandardSudokuBuilder(
    standardLayout: StandardLayout,
    messageBroker: SudokuMessageBroker,
    symbols: Symbols? = null
) : SudokuBuilder(
    sudoku = Sudoku.of(
        dimension = standardLayout.dimension,
        regionFuncs = standardLayout.regionFuncs,
        symbols = symbols ?: standardLayout.symbols
    ),
    messageBroker = messageBroker
) {
    enum class StandardLayout(
        val dimension: Dimension,
        val regionFuncs: List<RegionFunc>,
        val symbols: Symbols
    ) {
        STANDARD_9X9(DIMENSION_9, StandardRegions.regionFunctionsForDimension(DIMENSION_9), SYMBOLS_19),
        STANDARD_4X4(DIMENSION_4, StandardRegions.regionFunctionsForDimension(DIMENSION_4), SYMBOLS_14);

        companion object {
            fun of(dimension: Dimension): StandardLayout? =
                values().find { it.dimension == dimension }
        }
    }
}
