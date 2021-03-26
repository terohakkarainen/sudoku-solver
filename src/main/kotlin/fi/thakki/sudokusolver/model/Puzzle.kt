package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.roundToInt

typealias CellCoordinates = Set<Coordinates>
typealias RegionFunc = (Cells) -> CellCoordinates

@Serializable
class Puzzle(
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
            regions.single { region -> cell in region } // All cells must belong to a single region.
        }
        // TODO how to check that regions contain adjacent cells?
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
        ): Puzzle {
            require(regionFuncs.size == dimension.value) { "Number of region functions must match with dimension" }

            val cells = cellsForDimension(dimension, symbols)
            val coordinatesForRegions = regionFuncs.map { it(cells) }.toSet()

            return Puzzle(dimension, symbols, cells, coordinatesForRegions)
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
