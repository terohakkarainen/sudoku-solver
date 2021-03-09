package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.extensions.unsetCells
import fi.thakki.sudokusolver.model.Cell
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.StrongLinkType
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
            updateCandidates(),
            updateStrongLinks(),
            eliminateCandidates(),
            determineValues()
        ).contains(true)

    private fun updateCandidates(): Boolean =
        puzzle.cells.unsetCells()
            .map { cell -> updateCandidatesForCell(cell) }
            .contains(true)

    private fun updateCandidatesForCell(cell: Cell): Boolean =
        cell.analysis.candidates.let { existingCandidates ->
            puzzle.symbols.minus(symbolsTakenFromCellPerspective(cell)).let { newCandidates ->
                if (existingCandidates != newCandidates) {
                    PuzzleMutationService(puzzle).setCellCandidates(cell.coordinates, newCandidates)
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

    private fun updateStrongLinks(): Boolean {
        puzzle.cells.unsetCells().forEach { cell -> cell.analysis.strongLinks.clear() }
        puzzle.bands.forEach { band -> updateStrongLinksForCells(band, StrongLinkType.BAND) }
        puzzle.stacks.forEach { stack -> updateStrongLinksForCells(stack, StrongLinkType.STACK) }
        puzzle.regions.forEach { region -> updateStrongLinksForCells(region, StrongLinkType.REGION) }
        return false // Strong link update does not imply new analyze round.
    }

    private fun updateStrongLinksForCells(cells: Collection<Cell>, strongLinkType: StrongLinkType) {
        puzzle.symbols.forEach { symbol ->
            cells.unsetCells()
                .filter { it.analysis.candidates.contains(symbol) }
                .let { cellsWithSymbolCandidate ->
                    if (cellsWithSymbolCandidate.size == 2) {
                        PuzzleMutationService(puzzle).addStrongLink(
                            symbol,
                            cellsWithSymbolCandidate.first(),
                            cellsWithSymbolCandidate.last(),
                            strongLinkType
                        )
                    }
                }
        }
    }

    private fun eliminateCandidates(): Boolean =
        // TODO what to do here? Find strong links and eliminate using them?
        // If strong link in region in band/stack, eliminate candidates outside region.
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
                unsolvedCells.singleOrNull { cell -> cell.analysis.candidates.contains(symbol) }?.let {
                    invokeSetCellValue(
                        unsolvedCells.single { it.analysis.candidates.contains(symbol) },
                        symbol
                    )
                    true
                } ?: false
            }.contains(true)
        }

    private fun invokeSetCellValue(cell: Cell, value: Symbol) {
        PuzzleMutationService(puzzle).setCellValue(cell.coordinates, value)
    }
}
