package fi.thakki.sudokusolver.model

class Symbols(
    private val symbols: Set<Symbol>
) : Set<Symbol> by symbols {

    constructor(vararg symbols: Char) : this(symbols.toSet())

    fun isSupported(symbol: Symbol) =
        symbol in symbols
}
