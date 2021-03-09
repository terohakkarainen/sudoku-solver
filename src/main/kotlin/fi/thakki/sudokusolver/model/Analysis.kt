package fi.thakki.sudokusolver.model

enum class StrongLinkType {
    BAND,
    STACK,
    REGION
}

data class StrongLink(
    val symbol: Symbol,
    val toCell: Cell,
    val linkType: StrongLinkType
)

data class Analysis(
    val candidates: MutableSet<Symbol> = mutableSetOf(),
    val strongLinks: MutableSet<StrongLink> = mutableSetOf()
) {

    fun clear() {
        candidates.clear()
        strongLinks.clear()
    }
}
