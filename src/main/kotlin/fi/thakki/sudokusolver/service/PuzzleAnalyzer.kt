package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.extensions.unsetCells
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleTraverser
import java.time.Duration
import java.time.Instant

class PuzzleAnalyzer(private val puzzle: Puzzle) {

    fun analyze() {
        var round = 1
        val startingTime = Instant.now()
        while (true) {
            PuzzleMessageBroker.message("Analyzing puzzle (round $round)...")
            if (!runAnalyzeRound()) {
                val duration = Duration.between(startingTime, Instant.now()).toMillis()
                PuzzleMessageBroker.message("No new results, stopping analyze after ${duration}ms")
                return
            } else round++
        }
    }

    private fun runAnalyzeRound(): Boolean =
        listOf(
            eliminateCandidatesForCells(),
            eliminateCandidatesForCellCollections(),
            determineValues()
        ).contains(true)

    private fun eliminateCandidatesForCells(): Boolean =
        puzzle.cells.unsetCells()
            .map { cell ->
                eliminateCandidatesForCell(cell)
            }.contains(true)

    private fun eliminateCandidatesForCell(cell: Cell): Boolean =
        cell.analysis.candidates.let { existingCandidates ->
            puzzle.symbols.minus(symbolsTakenFromCellPerspective(cell)).let { newCandidates ->
                if (existingCandidates != newCandidates) {
                    invokeSetCellCandidates(cell, newCandidates)
                    true
                } else false
            }
        }

    private fun symbolsTakenFromCellPerspective(cell: Cell): Set<Symbol> =
        PuzzleTraverser(puzzle).let { puzzleTraverser ->
            listOf(
                puzzleTraverser::bandOf,
                puzzleTraverser::stackOf,
                puzzleTraverser::regionOf
            ).map { traverseFunc -> traverseFunc(cell).mapNotNull { it.value }.toSet() }
                .reduce { acc, s -> acc.union(s) }
        }

    private fun eliminateCandidatesForCellCollections(): Boolean =
        // TODO what to do here? Find strong links and eliminate using them?
        false

    private fun determineValues(): Boolean {
        val setterFuncs = listOf(
            { puzzle.cells.unsetCells().map { cell -> setValueIfCellContainsOnlyOneCandidate(cell) } },
            { puzzle.bands.map { band -> setValueIfCandidateOccursInOneCellOnly(band) } },
            { puzzle.stacks.map { stack -> setValueIfCandidateOccursInOneCellOnly(stack) } },
            { puzzle.regions.map { region -> setValueIfCandidateOccursInOneCellOnly(region) } }
        ).iterator()

        while (setterFuncs.hasNext()) {
            if (setterFuncs.next().invoke().contains(true)) {
                // Need to abstain from setting more values before candidates are updated.
                return true
            }
        }
        return false
    }

    private fun setValueIfCellContainsOnlyOneCandidate(cell: Cell): Boolean =
        cell.analysis.candidates.singleOrNull()?.let { singleCandidate ->
            invokeSetCellValue(cell, singleCandidate)
            PuzzleMessageBroker.message("Cell ${cell.coordinates} value set to $singleCandidate")
            true
        } ?: false

    private fun setValueIfCandidateOccursInOneCellOnly(cells: Collection<Cell>): Boolean =
        cells.unsetCells().let { unsolvedCells ->
            puzzle.symbols.map { symbol ->
                if (1 == unsolvedCells.count { cell ->
                        cell.analysis.candidates.contains(symbol)
                    }) {
                    invokeSetCellValue(
                        unsolvedCells.single { it.analysis.candidates.contains(symbol) },
                        symbol
                    )
                    true
                } else false
            }.contains(true)
        }

    private fun invokeSetCellValue(cell: Cell, value: Symbol) {
        PuzzleMutationService(puzzle).setCellValue(cell.coordinates, value)
    }

    private fun invokeSetCellCandidates(cell: Cell, candidates: Set<Symbol>) {
        PuzzleMutationService(puzzle).setCellCandidates(cell.coordinates, candidates)
    }
}
