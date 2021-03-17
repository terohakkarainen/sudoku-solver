package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.PuzzleMessageBroker
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

    fun analyze(rounds: Int = DEFAULT_ANALYZE_ROUNDS) {
        var round = 1
        val startingTime = Instant.now()

        // Initialization for first round.
        SimpleCandidateUpdater(puzzle).updateCandidates()
        StrongLinkUpdater(puzzle).updateStrongLinks()

        while (round <= rounds) {
            PuzzleMessageBroker.message("Analyzing puzzle (round $round)...")
            when (runAnalyzeRound()) {
                is AnalyzeResult.ValueSet -> round++
                is AnalyzeResult.CandidatesEliminated -> round++
                is AnalyzeResult.NoChanges -> {
                    PuzzleMessageBroker.message(
                        "No new results from round $round, " +
                                "stopping analyze after ${milliSecondsSince(startingTime)}ms, " +
                                "${puzzle.readinessPercentage()}% complete."
                    )
                    return
                }
            }
        }
        StrongLinkUpdater(puzzle).updateStrongLinks()
        PuzzleMessageBroker.message(
            "Analyzed $rounds round(s), which took ${milliSecondsSince(startingTime)}ms, " +
                    "${puzzle.readinessPercentage()}% complete."
        )
    }

    private fun milliSecondsSince(instant: Instant) =
        Duration.between(instant, Instant.now()).toMillis()

    private fun runAnalyzeRound(): AnalyzeResult =
        CellValueDeducer(puzzle).deduceSomeValue().let { deduceResult ->
            if (deduceResult is AnalyzeResult.ValueSet) {
                SimpleCandidateUpdater(puzzle).updateCandidates()
                deduceResult
            } else {
                CandidateClusterFinder(puzzle).findClusters()
                StrongLinkUpdater(puzzle).updateStrongLinks()
                StrongLinkCandidateEliminator(puzzle).eliminateCandidates()
            }
        }

    fun updateCandidatesOnly() {
        when (SimpleCandidateUpdater(puzzle).updateCandidates()) {
            AnalyzeResult.CandidatesEliminated -> PuzzleMessageBroker.message("Some candidates removed")
            else -> PuzzleMessageBroker.message("No changes made")
        }
    }

    fun updateStrongLinksOnly() {
        StrongLinkUpdater(puzzle).updateStrongLinks()
        PuzzleMessageBroker.message("Strong links rebuilt")
    }

    fun eliminateCandidatesOnly() {
        StrongLinkUpdater(puzzle).updateStrongLinks()
        when (StrongLinkCandidateEliminator(puzzle).eliminateCandidates()) {
            AnalyzeResult.CandidatesEliminated -> PuzzleMessageBroker.message("Some candidates removed")
            else -> PuzzleMessageBroker.message("No changes made")
        }
    }

    fun deduceValuesOnly() {
        // First update candidates so that deduce doesn't make stupid mistakes.
        SimpleCandidateUpdater(puzzle).updateCandidates()
        when (val result = CellValueDeducer(puzzle).deduceSomeValue()) {
            is AnalyzeResult.ValueSet -> {
                SimpleCandidateUpdater(puzzle).updateCandidates()
                PuzzleMessageBroker.message("Value ${result.value} set to cell ${result.coordinates}")
            }
            else -> PuzzleMessageBroker.message("No value could be deduced")
        }
    }

    companion object {
        const val DEFAULT_ANALYZE_ROUNDS = 1
    }
}
