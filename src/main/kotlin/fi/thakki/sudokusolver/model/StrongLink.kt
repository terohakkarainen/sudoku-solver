package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable

enum class StrongLinkType {
    BAND,
    STACK,
    REGION
}

@Serializable
data class StrongLink(
    val symbol: Symbol,
    val firstCell: Cell,
    val secondCell: Cell
) {
    fun cells(): Set<Cell> =
        setOf(firstCell, secondCell)

    fun reverse(): StrongLink =
        copy(firstCell = secondCell, secondCell = firstCell)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StrongLink
        return when {
            symbol != other.symbol -> false
            firstCell != other.firstCell -> false
            secondCell != other.secondCell -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + firstCell.hashCode()
        result = 31 * result + secondCell.hashCode()
        return result
    }

    override fun toString(): String =
        "${firstCell.coordinates} - ${secondCell.coordinates} [$symbol]"
}
