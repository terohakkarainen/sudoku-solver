package fi.thakki.sudokusolver.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class CellValueType {
    SETTABLE,
    GIVEN
}

@Serializable
data class Cell(val coordinates: Coordinates, private val symbols: Symbols) {

    @Serializable
    data class Analysis(
        var candidates: Set<Symbol> = emptySet(),
        @Transient
        var strongLinks: Set<StrongLink> = emptySet()
    )

    var value: Symbol? = null
        set(newValue) {
            field = newValue
            analysis.candidates = if (newValue != null) emptySet() else symbols
            analysis.strongLinks = emptySet()
        }
    var type: CellValueType = CellValueType.SETTABLE
    val analysis = Analysis(candidates = symbols)

    fun hasValue(): Boolean =
        value != null

    fun setGiven(value: Symbol) {
        this.value = value
        type = CellValueType.GIVEN
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Cell
        return coordinates == other.coordinates
    }

    override fun hashCode(): Int = coordinates.hashCode()

    override fun toString(): String =
        "Cell$coordinates=$value"
}
