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

    private data class RepeatedAnalyzeResult(
        val completed: Boolean,
        val roundResults: List<AnalyzeResult>
    )

    fun analyze(
        rounds: Int = DEFAULT_ANALYZE_ROUNDS,
        doHeuristicAnalysis: Boolean = true
    ): AnalyzeResult {
        val startingTime = Instant.now()

        // Initialization for first round.
        SimpleCandidateUpdater(puzzle).updateCandidates()
        StrongLinkUpdater(puzzle).updateStrongLinks()

        val result = analyzeAtMostRounds(rounds, doHeuristicAnalysis)

        if (result.roundResults.last() == AnalyzeResult.NoChanges) {
            PuzzleMessageBroker.message(
                "No new results from round ${result.roundResults.size}, " +
                        "stopping analyze after ${milliSecondsSince(startingTime)}ms, " +
                        "${puzzle.readinessPercentage()}% complete."
            )
        } else {
            StrongLinkUpdater(puzzle).updateStrongLinks()
            PuzzleMessageBroker.message(
                "Analyzed ${result.roundResults.size} round(s), which took ${milliSecondsSince(startingTime)}ms, " +
                        "${puzzle.readinessPercentage()}% complete."
            )
        }

        return AnalyzeResult.combinedResultOf(result.roundResults)
    }

    private fun milliSecondsSince(instant: Instant) =
        Duration.between(instant, Instant.now()).toMillis()

    private fun analyzeAtMostRounds(maxRounds: Int, doHeuristicAnalysis: Boolean): RepeatedAnalyzeResult {
        var round = 1
        val results = mutableListOf<AnalyzeResult>()

        while (round <= maxRounds && (results.isEmpty() || results.last() != AnalyzeResult.NoChanges)) {
            PuzzleMessageBroker.message("Analyzing puzzle (round $round)...")
            results.add(runAnalyzeRound(doHeuristicAnalysis))
            round++
        }

        return RepeatedAnalyzeResult(
            completed = round == maxRounds,
            roundResults = results
        )
    }

    private fun runAnalyzeRound(doHeuristicAnalysis: Boolean): AnalyzeResult =
        CellValueDeducer(puzzle).deduceSomeValue().let { deduceResult ->
            if (deduceResult is AnalyzeResult.ValueSet) {
                SimpleCandidateUpdater(puzzle).updateCandidates()
                deduceResult
            } else {
                AnalyzeResult.combinedResultOf(
                    listOf(
                        CandidateBasedCandidateEliminator(puzzle).eliminateCandidates(),
                        CandidateClusterFinder(puzzle).findClusters(),
                        StrongLinkUpdater(puzzle).updateStrongLinks(),
                        StrongLinkBasedCandidateEliminator(puzzle).eliminateCandidates(),
                        StrongLinkChainBasedCandidateEliminator(puzzle).eliminateCandidates(),
                        if (doHeuristicAnalysis) {
                            HeuristicCandidateEliminator(puzzle).eliminateCandidates()
                        } else AnalyzeResult.NoChanges
                    )
                )
            }
        }

    fun updateCandidatesOnly(): AnalyzeResult =
        SimpleCandidateUpdater(puzzle).updateCandidates()
            .also { result ->
                PuzzleMessageBroker.message(
                    when (result) {
                        AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                        else -> "No changes made"
                    }
                )
            }

    fun updateStrongLinksOnly(): AnalyzeResult =
        StrongLinkUpdater(puzzle).updateStrongLinks()
            .also {
                PuzzleMessageBroker.message("Strong links rebuilt")
            }

    fun eliminateCandidatesOnly(): AnalyzeResult =
        StrongLinkBasedCandidateEliminator(puzzle).eliminateCandidates()
            .also { result ->
                PuzzleMessageBroker.message(
                    when (result) {
                        AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                        else -> "No changes made"
                    }
                )
            }

    fun deduceValuesOnly(): AnalyzeResult {
        // First update candidates so that deduce doesn't make stupid mistakes.
        SimpleCandidateUpdater(puzzle).updateCandidates()
        return CellValueDeducer(puzzle).deduceSomeValue()
            .also { result ->
                when (result) {
                    is AnalyzeResult.ValueSet -> {
                        SimpleCandidateUpdater(puzzle).updateCandidates()
                        PuzzleMessageBroker.message("Value ${result.value} set to cell ${result.coordinates}")
                    }
                    else -> PuzzleMessageBroker.message("No value could be deduced")
                }
            }
    }

    companion object {
        const val DEFAULT_ANALYZE_ROUNDS = 1
    }
}
