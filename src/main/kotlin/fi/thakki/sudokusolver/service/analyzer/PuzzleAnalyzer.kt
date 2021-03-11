package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
import fi.thakki.sudokusolver.service.PuzzleMutationService
import fi.thakki.sudokusolver.util.PuzzleTraverser
import java.time.Duration
import java.time.Instant

sealed class AnalyzeResult {
    object NoChanges : AnalyzeResult()
    object CandidatesEliminated : AnalyzeResult()
    data class ValueSet(val value: Symbol, val coordinates: Coordinates) : AnalyzeResult()

    companion object {
        fun combinedResultOf(results: Collection<AnalyzeResult>): AnalyzeResult =
            when {
                results.any { it is ValueSet } -> {
                    checkNotNull(results.find { it is ValueSet })
                }
                results.any { it is CandidatesEliminated } -> CandidatesEliminated
                else -> NoChanges
            }
    }
}

class PuzzleAnalyzer(private val puzzle: Puzzle) {

    fun analyze() {
        var round = 1
        val startingTime = Instant.now()
        while (true) {
            PuzzleMessageBroker.message("Analyzing puzzle (round $round)...")
            when (val analyzeResult = runAnalyzeRound()) {
                is AnalyzeResult.ValueSet -> {
                    removeSetValueFromCandidates(analyzeResult.coordinates, analyzeResult.value)
                    round++
                }
                is AnalyzeResult.CandidatesEliminated -> round++
                is AnalyzeResult.NoChanges -> {
                    val duration = Duration.between(startingTime, Instant.now()).toMillis()
                    PuzzleMessageBroker.message(
                        "No new results from round $round, stopping analyze after ${duration}ms, " +
                                "${puzzle.readinessPercentage()}% complete."
                    )
                    return
                }
            }
        }
    }

    private fun removeSetValueFromCandidates(coordinates: Coordinates, value: Symbol) {
        val puzzleTraverser = PuzzleTraverser(puzzle)
        val puzzleMutationService = PuzzleMutationService(puzzle)
        val cell = puzzleTraverser.cellAt(coordinates)

        listOf(
            puzzleTraverser::regionOf,
            puzzleTraverser::bandOf,
            puzzleTraverser::stackOf
        ).forEach { traverserFunc ->
            traverserFunc(cell).unsetCells().forEach { cellToUnset ->
                if (cellToUnset.analysis.candidates.contains(value)) {
                    puzzleMutationService.setCellCandidates(
                        cellToUnset.coordinates,
                        cellToUnset.analysis.candidates.minus(value)
                    )
                }
            }
        }
        // Strong links are always reset, so no need to process them.
    }

    private fun runAnalyzeRound(): AnalyzeResult =
        AnalyzeResult.combinedResultOf(
            listOf(
                SimpleCandidateUpdater(puzzle).updateCandidates(),
                StrongLinkUpdater(puzzle).updateStrongLinks(),
                StrongLinkCandidateEliminator(puzzle).eliminateCandidates(),
                CellValueDeducer(puzzle).deduceSomeValue()
            )
        )
}
