package fi.thakki.sudokusolver.model

data class Analysis(
    val candidates: MutableSet<Symbol> = mutableSetOf()
)
