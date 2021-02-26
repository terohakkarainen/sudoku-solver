package fi.thakki.sudokusolver.extensions

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Symbol

fun Collection<Cell>.containsSymbol(symbol: Symbol): Boolean =
    find { it.value == symbol } != null
