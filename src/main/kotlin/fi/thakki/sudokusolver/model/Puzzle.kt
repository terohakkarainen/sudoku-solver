package fi.thakki.sudokusolver.model

import kotlin.math.roundToInt

typealias RegionFunc = (Puzzle) -> Region

class Puzzle(
    val dimension: Dimension,
    regionFuncs: List<RegionFunc>,
    val symbols: Symbols
) {
    val cells: Cells
    val bands: List<Band>
    val stacks: List<Stack>
    val regions: Set<Region>

    init {
        require(regionFuncs.size == dimension.value) { "Number of region functions must match with dimension" }
        require(symbols.size == dimension.value) { "Number of symbols must match with dimension" }
        cells = cellsForDimension(dimension, symbols)
        check(cells.size == dimension.value * dimension.value) {
            "Number of cells did not match with dimension square"
        }
        bands = initializeBands(cells, dimension)
        check(bands.size == dimension.value) { "Number of bands did not match with dimension" }
        stacks = initializeStacks(cells, dimension)
        check(stacks.size == dimension.value) { "Number of stacks did not match with dimension" }
        regions = regionFuncs.map { it(this) }.toSet()
        check(regions.size == dimension.value) { "Number of regions did not match with dimension" }
        regions.forEach { region ->
            check(region.size == dimension.value) { "Region size did not match dimension" }
        }
        cells.forEach { cell ->
            regions.single { region -> cell in region } // All cells must belong to a single region.
        }
    }

    private fun cellsForDimension(dimension: Dimension, symbols: Symbols): Cells =
        Cells(
            (0 until dimension.value).flatMap { x ->
                (0 until dimension.value).map { y ->
                    Pair(x, y)
                }
            }.map { xyPair ->
                Cell(Coordinates(xyPair.first, xyPair.second), symbols)
            }.toSet()
        )

    private fun initializeBands(cells: Set<Cell>, dimension: Dimension): List<Band> =
        (0 until dimension.value).map { y ->
            Band(cells.filter { it.coordinates.y == y })
        }

    private fun initializeStacks(cells: Set<Cell>, dimension: Dimension): List<Stack> =
        (0 until dimension.value).map { x ->
            Stack(cells.filter { it.coordinates.x == x })
        }

    fun isComplete(): Boolean =
        cells.unsetCells().isEmpty()

    @Suppress("MagicNumber")
    fun readinessPercentage(): Int =
        ((cells.size - cells.unsetCells().size).toDouble() / cells.size.toDouble() * 100f).roundToInt()

    fun allCellCollections(): List<CellCollection> =
        listOf(bands, stacks, regions).flatten()

    fun isSupportedSymbol(symbol: Symbol) =
        symbol in symbols
}
