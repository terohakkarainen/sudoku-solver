package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable

@Serializable
class Symbols(
    private val symbols: Set<Symbol>
) : Set<Symbol> by symbols {

    constructor(vararg symbols: Char) : this(symbols.toSet())

    fun isSupported(symbol: Symbol) =
        symbol in symbols
}
