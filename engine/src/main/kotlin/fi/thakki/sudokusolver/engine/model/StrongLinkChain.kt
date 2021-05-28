package fi.thakki.sudokusolver.engine.model

data class StrongLinkChain(
    val symbol: Symbol,
    val strongLinks: List<StrongLink>
) : List<StrongLink> by strongLinks {

    init {
        require(isAcceptableChainLength(strongLinks.size)) {
            "Strong link chain size must be odd and greater or equal to 3"
        }
        require(strongLinks.distinct().size == strongLinks.size) {
            "Strong link chain must not contain the same strong link twice"
        }
        require(isContinuous()) { "Strong link chain must be continuous" }
        require(strongLinks.all { it.symbol == symbol }) { "All strong links in chain must share the same symbol" }
        require(areNotCircular(strongLinks)) { "Strong link chain must not be circular" }
    }

    private fun isContinuous(): Boolean =
        strongLinks.zipWithNext { previousLink, nextLink ->
            previousLink.secondCell == nextLink.firstCell
        }.all { it }

    override fun equals(other: Any?): Boolean {
        fun cellsOf(strongLinks: Collection<StrongLink>): Set<Cell> =
            strongLinks.flatMap { it.cells() }.toSet()

        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StrongLinkChain
        return when {
            symbol != other.symbol -> false
            cellsOf(strongLinks) != cellsOf(other.strongLinks) -> false
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

    companion object {
        private const val MINIMUM_CHAIN_LENGTH = 3

        // Chain must contain an even number of nodes for the chain to be effective
        // in candidate elimination.
        fun isAcceptableChainLength(length: Int): Boolean =
            length >= MINIMUM_CHAIN_LENGTH && length % 2 == 1

        fun areNotCircular(strongLinks: List<StrongLink>): Boolean =
            // If chain was circular, some cell would exist twice in chain.
            strongLinks.size + 1 == strongLinks.flatMap { link -> link.cells() }.toSet().size
    }
}
