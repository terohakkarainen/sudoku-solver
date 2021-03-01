package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.command.SetCellGivenCommand
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.RegionFunc
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.model.Symbols
import fi.thakki.sudokusolver.service.CommandExecutorService
import fi.thakki.sudokusolver.util.StandardDimensions.DIMENSION_4
import fi.thakki.sudokusolver.util.StandardDimensions.DIMENSION_9
import fi.thakki.sudokusolver.util.StandardSymbols.SYMBOLS_14
import fi.thakki.sudokusolver.util.StandardSymbols.SYMBOLS_19

class PuzzleBuilder(layout: Layout) {

    @Suppress("unused")
    enum class Layout(
        val dimension: Dimension,
        val regionFuncs: List<RegionFunc>,
        val symbols: Symbols
    ) {
        STANDARD_9X9(DIMENSION_9, StandardRegions.regionFunctionsForDimension(DIMENSION_9), SYMBOLS_19),
        STANDARD_4X4(DIMENSION_4, StandardRegions.regionFunctionsForDimension(DIMENSION_4), SYMBOLS_14)
    }

    private val puzzle: Puzzle = Puzzle(
        dimension = layout.dimension,
        regionFuncs = layout.regionFuncs,
        symbols = layout.symbols
    )

    fun withGiven(symbol: Symbol, coordinates: Coordinates): PuzzleBuilder {
        CommandExecutorService.executeCommandOnPuzzle(
            SetCellGivenCommand(coordinates, symbol),
            puzzle
        )
        return this
    }

    fun build() = puzzle
}