package fi.thakki.sudokusolver.model

data class StrongLink(
    val symbol: Symbol,
    val firstCell: Cell,
    val secondCell: Cell,
    val linkType: LinkType
) {

    enum class LinkType {
        BAND,
        STACK,
        REGION
    }
}
