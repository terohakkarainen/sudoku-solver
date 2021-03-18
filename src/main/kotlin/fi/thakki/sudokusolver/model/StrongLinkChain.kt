package fi.thakki.sudokusolver.model

data class StrongLinkChain(
    val symbol: Symbol,
    val strongLinks: List<StrongLink>
) : List<StrongLink> by strongLinks {

    init {
        require(strongLinks.size >= 2) { "Strong link chain must contain at least two links" }
        require(strongLinks.all { it.symbol == symbol }) { "All strong links in chain must share the same symbol" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StrongLinkChain
        return when {
            symbol != other.symbol -> false
            strongLinks != other.strongLinks -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + strongLinks.hashCode()
        return result
    }

    override fun toString(): String {
        var result = ""
        strongLinks.iterator().let { iter ->
            while (iter.hasNext()) {
                result += iter.next()
                if (iter.hasNext()) result += " -> "
            }
        }
        return result
    }
}
