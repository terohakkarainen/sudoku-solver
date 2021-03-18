package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Dimension
import fi.thakki.sudokusolver.model.RegionFunc
import kotlin.math.roundToInt
import kotlin.math.sqrt

object StandardRegions {

    fun regionFunctionsForDimension(dimension: Dimension): List<RegionFunc> =
        getRegionRanges(dimension)
            .let { regionRanges ->
                regionRanges.flatMap { xRange ->
                    regionRanges.map { yRange ->
                        Pair(xRange, yRange)
                    }
                }.map { rangePair ->
                    regionFuncOf(
                        xRangePredicate(rangePair.first),
                        yRangePredicate(rangePair.second)
                    )
                }
            }

    private fun getRegionRanges(dimension: Dimension): List<IntRange> =
        sqrt(dimension.value.toDouble()).roundToInt().let { regionSizeAndCount ->
            (0 until regionSizeAndCount).map { index ->
                IntRange(index * regionSizeAndCount, index * regionSizeAndCount + regionSizeAndCount - 1)
            }
        }

    private fun regionFuncOf(xFilter: (Coordinates) -> Boolean, yFilter: (Coordinates) -> Boolean): RegionFunc =
        { cells -> cells.cells.map { it.coordinates }.filter(xFilter).filter(yFilter).toSet() }

    private fun xRangePredicate(coordinateRange: IntRange): (Coordinates) -> Boolean =
        { coordinates -> coordinates.x in coordinateRange }

    private fun yRangePredicate(coordinateRange: IntRange): (Coordinates) -> Boolean =
        { coordinates -> coordinates.y in coordinateRange }
}
