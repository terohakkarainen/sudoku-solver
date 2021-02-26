package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.domain.Coordinates
import fi.thakki.sudokusolver.domain.Dimension
import fi.thakki.sudokusolver.domain.Puzzle
import fi.thakki.sudokusolver.domain.RegionFunc
import fi.thakki.sudokusolver.domain.Symbol

class PuzzleBuilder(layout: Layout) {

    @Suppress("unused")
    enum class Layout(val dimension: Dimension, val regionFuncs: List<RegionFunc>) {
        STANDARD_9X9(DIMENSION_9, StandardRegions.regionFunctionsForDimension(DIMENSION_9)),
        STANDARD_4X4(DIMENSION_4, StandardRegions.regionFunctionsForDimension(DIMENSION_4))
    }

    private val puzzle: Puzzle = Puzzle(
        dimension = layout.dimension,
        regionFuncs = layout.regionFuncs
    )

    fun withGiven(symbol: Symbol, coordinates: Coordinates): PuzzleBuilder {
        puzzle.cellAt(coordinates).setGiven(symbol)
        return this
    }

    fun build() = puzzle

    companion object {
        private val DIMENSION_9 = Dimension(9)
        private val DIMENSION_4 = Dimension(4)
    }
}
