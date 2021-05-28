package fi.thakki.sudokusolver.engine.model

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
            cells() != other.cells() -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + cells().hashCode()
        return result
    }

    override fun toString(): String =
        "${firstCell.coordinates} - ${secondCell.coordinates} [$symbol]"
}
