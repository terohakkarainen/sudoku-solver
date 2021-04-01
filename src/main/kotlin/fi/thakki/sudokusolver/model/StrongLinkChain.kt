package fi.thakki.sudokusolver.model

data class StrongLinkChain(
    val symbol: Symbol,
    val strongLinks: List<StrongLink>
) : List<StrongLink> by strongLinks {

    init {
        // TODO chain should be at least 3 links long (for it to be useful).
        require(strongLinks.size >= 2) { "Strong link chain must contain at least two links" }
        require(strongLinks.distinct().size == strongLinks.size) {
            "Strong link chain must not contain the same strong link twice"
        }
        require(isChainContinuous()) { "Strong link chain must be continuous" }
        require(strongLinks.all { it.symbol == symbol }) { "All strong links in chain must share the same symbol" }
        require(strongLinks.size + 1 == strongLinks.flatMap { link -> link.cells() }.distinct().size) {
            "Strong link chain must not visit the same cell twice"
        }
    }

    // TODO refactor with fold?
    private fun isChainContinuous(): Boolean {
        var previous: StrongLink? = null
        strongLinks.forEach { strongLink ->
            previous?.let {
                if (it.secondCell != strongLink.firstCell) return false
            }
            previous = strongLink
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StrongLinkChain
        return when {
            symbol != other.symbol -> false
            strongLinks.toSet() != other.strongLinks.toSet() -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + strongLinks.toSet().hashCode()
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
