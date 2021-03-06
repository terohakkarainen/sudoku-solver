package fi.thakki.sudokusolver.model

data class Analysis(
    var candidates: Set<Symbol> = emptySet()
)
