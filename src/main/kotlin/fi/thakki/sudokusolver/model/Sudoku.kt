package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.abs
import kotlin.math.roundToInt

typealias CellCoordinates = Set<Coordinates>
typealias RegionFunc = (Cells) -> CellCoordinates

@Serializable
class Sudoku(
    val dimension: Dimension,
    val symbols: Symbols,
    val cells: Cells,
    @Suppress("CanBeParameter") // Needs to be field for serialization to work.
    val coordinatesForRegions: Set<CellCoordinates>
) {
    enum class State {
        NOT_ANALYZED_YET,
        ANALYZED,
        COMPLETE
    }

    data class Analysis(
        var strongLinkChains: Set<StrongLinkChain> = emptySet()
    )

    @Transient
    val analysis = Analysis()

    @Transient
    val bands: List<Band> = initializeBands(cells, dimension)

    @Transient
    val stacks: List<Stack> = initializeStacks(cells, dimension)

    @Transient
    val regions: Set<Region> = initializeRegions(cells, coordinatesForRegions)

    @Transient
    var revision: String? = null

    var state: State = State.NOT_ANALYZED_YET

    init {
        require(symbols.size == dimension.value) { "Number of symbols must match with dimension" }
        check(cells.size == dimension.value * dimension.value) {
            "Number of cells did not match with dimension square"
        }
        check(bands.size == dimension.value) { "Number of bands did not match with dimension" }
        check(stacks.size == dimension.value) { "Number of stacks did not match with dimension" }
        check(regions.size == dimension.value) { "Number of regions did not match with dimension" }
        regions.forEach { region ->
            check(region.size == dimension.value) { "Region size did not match dimension" }
        }
        cells.forEach { cell ->
            checkNotNull(regions.singleOrNull { region -> cell in region }) {
                "Cell ${cell.coordinates} belongs to multiple regions or no region"
            }
        }
        regions.forEach { region ->
            region.cells.forEach { cell ->
                check(
                    region.cells.minus(cell).any { otherCell ->
                        areAdjacent(cell, otherCell)
                    }
                ) {
                    "Cell ${cell.coordinates} is not adjacent to any other cell in region"
                }
            }
        }
    }

    private fun areAdjacent(first: Cell, second: Cell): Boolean =
        coordinateDifference(first, second).let { diff ->
            when {
                diff.width == 0 -> diff.height == 1
                diff.height == 0 -> diff.width == 1
                else -> false
            }
        }

    private fun coordinateDifference(first: Cell, second: Cell): Size {
        fun absoluteDiff(i: Int, j: Int) = abs(i - j)
        return Size(
            absoluteDiff(first.coordinates.x, second.coordinates.x),
            absoluteDiff(first.coordinates.y, second.coordinates.y)
        )
    }

    private fun initializeBands(cells: Set<Cell>, dimension: Dimension): List<Band> =
        (0 until dimension.value).map { y ->
            Band(cells.filter { it.coordinates.y == y })
        }

    private fun initializeStacks(cells: Set<Cell>, dimension: Dimension): List<Stack> =
        (0 until dimension.value).map { x ->
            Stack(cells.filter { it.coordinates.x == x })
        }

    private fun initializeRegions(cells: Set<Cell>, coordinatesForRegions: Set<CellCoordinates>): Set<Region> =
        coordinatesForRegions.map { regionCoordinates ->
            Region(cells.filter { it.coordinates in regionCoordinates }.toSet())
        }.toSet()

    @Suppress("MagicNumber")
    fun readinessPercentage(): Int =
        ((cells.size - cells.cellsWithoutValue().size).toDouble() / cells.size.toDouble() * 100f).roundToInt()

    fun allCellCollections(): List<CellCollection> =
        listOf(bands, stacks, regions).flatten()

    companion object {
        fun of(
            dimension: Dimension,
            symbols: Symbols,
            regionFuncs: List<RegionFunc>
        ): Sudoku {
            require(regionFuncs.size == dimension.value) { "Number of region functions must match with dimension" }

            val cells = cellsForDimension(dimension, symbols)
            val coordinatesForRegions = regionFuncs.map { it(cells) }.toSet()

            return Sudoku(dimension, symbols, cells, coordinatesForRegions)
        }

        fun cellsForDimension(dimension: Dimension, symbols: Symbols): Cells =
            Cells(
                (0 until dimension.value).flatMap { x ->
                    (0 until dimension.value).map { y ->
                        Pair(x, y)
                    }
                }.map { xyPair ->
                    Cell(Coordinates(xyPair.first, xyPair.second), symbols)
                }.toSet()
            )
    }
}
