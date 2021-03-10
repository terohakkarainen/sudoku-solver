package fi.thakki.sudokusolver.model

abstract class CellCollection {

    data class Analysis(
        var strongLinks: Set<StrongLink> = emptySet()
    )

    val analysis = Analysis()
    abstract val cells: Collection<Cell>
}
