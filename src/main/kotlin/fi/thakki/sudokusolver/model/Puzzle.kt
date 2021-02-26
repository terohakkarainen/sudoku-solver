package fi.thakki.sudokusolver.model

typealias Cells = Set<Cell>
typealias Band = List<Cell>
typealias Stack = List<Cell>
typealias Region = Set<Cell>
typealias RegionFunc = (Puzzle) -> Region

class Puzzle(
    dimension: Dimension,
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
        cells = cellsForDimension(dimension)
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
            regionOf(cell) // check each cell belongs to a single region
        }
    }

    private fun cellsForDimension(dimension: Dimension): Cells =
        (0 until dimension.value).flatMap { x ->
            (0 until dimension.value).map { y ->
                Pair(x, y)
            }
        }.map { xyPair ->
            Cell(Coordinates(xyPair.first, xyPair.second))
        }.toSet()

    private fun initializeBands(cells: Cells, dimension: Dimension): List<Band> =
        (0 until dimension.value).map { y ->
            cells.filter { it.coordinates.y == y }.toList()
        }

    private fun initializeStacks(cells: Cells, dimension: Dimension): List<Stack> =
        (0 until dimension.value).map { x ->
            cells.filter { it.coordinates.x == x }.toList()
        }

    fun cellAt(coordinates: Coordinates): Cell =
        intersectionOf(
            bands[coordinates.y],
            stacks[coordinates.x]
        )

    private fun intersectionOf(band: Band, stack: Stack): Cell =
        checkNotNull(
            band.find {
                it.coordinates.x == stack.first().coordinates.x
            }
        )

    fun bandOf(cell: Cell) =
        bands[cell.coordinates.y]

    fun stackOf(cell: Cell) =
        stacks[cell.coordinates.x]

    fun regionOf(cell: Cell): Region =
        regions.single { region -> cell in region }
}
