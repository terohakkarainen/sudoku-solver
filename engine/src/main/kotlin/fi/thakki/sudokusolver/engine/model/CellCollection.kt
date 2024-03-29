package fi.thakki.sudokusolver.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
abstract class CellCollection {

    data class Analysis(
        var strongLinks: Set<StrongLink> = emptySet()
    )

    @Transient
    val analysis = Analysis()

    abstract val cells: Collection<Cell>

    fun containsSymbol(symbol: Symbol): Boolean =
        cells.any { it.value == symbol }

    fun cellsWithValue(): Set<Cell> =
        cells.filter { it.hasValue() }.toSet()

    fun cellsWithoutValue(): Set<Cell> =
        cells.filter { !it.hasValue() }.toSet()
}
