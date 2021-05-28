package fi.thakki.sudokusolver.engine.service.builder

import fi.thakki.sudokusolver.engine.model.Dimension
import fi.thakki.sudokusolver.engine.model.RegionFunc
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbols
import fi.thakki.sudokusolver.engine.service.builder.StandardDimensions.DIMENSION_4
import fi.thakki.sudokusolver.engine.service.builder.StandardDimensions.DIMENSION_9
import fi.thakki.sudokusolver.engine.service.builder.StandardSymbols.SYMBOLS_14
import fi.thakki.sudokusolver.engine.service.builder.StandardSymbols.SYMBOLS_19
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker

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
