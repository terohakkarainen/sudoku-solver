package fi.thakki.sudokusolver.model

enum class StrongLinkType {
    BAND,
    STACK,
    REGION
}

data class StrongLink(
    val symbol: Symbol,
    val firstCell: Cell,
    val secondCell: Cell
)
