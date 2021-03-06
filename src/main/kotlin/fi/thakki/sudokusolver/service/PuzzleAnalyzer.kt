package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser

class PuzzleAnalyzer(private val puzzle: Puzzle) {

    fun analyze(cell: Cell) {
        cell.analysis.candidates = puzzle.symbols.minus(valuesAffectingCell(cell))
    }

    private fun valuesAffectingCell(cell: Cell): Set<Symbol> =
        PuzzleTraverser(puzzle).let { puzzleTraverser ->
            listOf(
                puzzleTraverser::bandOf,
                puzzleTraverser::stackOf,
                puzzleTraverser::regionOf
            ).map { traverseFunc -> traverseFunc(cell).mapNotNull { it.value }.toSet() }
                .reduce { acc, s -> acc.union(s) }
        }
}
