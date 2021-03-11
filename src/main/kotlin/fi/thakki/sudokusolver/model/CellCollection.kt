package fi.thakki.sudokusolver.model

abstract class CellCollection {

    data class Analysis(
        var strongLinks: Set<StrongLink> = emptySet()
    )

    val analysis = Analysis()
    abstract val cells: Collection<Cell>

    fun containsSymbol(symbol: Symbol): Boolean =
        cells.any { it.value == symbol }

    fun unsetCells(): Set<Cell> =
        cells.filter { !it.hasValue() }.toSet()
}
