package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.PuzzleMessageBroker
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

class PuzzleAnalyzer(
    private val puzzle: Puzzle,
    private val messageBroker: PuzzleMessageBroker
) {

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
        SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
        StrongLinkUpdater(puzzle, messageBroker).updateStrongLinks()

        val result = analyzeAtMostRounds(rounds, doHeuristicAnalysis)

        if (result.roundResults.last() == AnalyzeResult.NoChanges) {
            messageBroker.message(
                "No new results from round ${result.roundResults.size}, " +
                        "stopping analyze after ${milliSecondsSince(startingTime)}ms, " +
                        "${puzzle.readinessPercentage()}% complete."
            )
        } else {
            StrongLinkUpdater(puzzle, messageBroker).updateStrongLinks()
            messageBroker.message(
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
            messageBroker.message("Analyzing puzzle (round $round)...")
            results.add(runAnalyzeRound(doHeuristicAnalysis))
            round++
        }

        return RepeatedAnalyzeResult(
            completed = round == maxRounds,
            roundResults = results
        )
    }

    private fun runAnalyzeRound(doHeuristicAnalysis: Boolean): AnalyzeResult =
        CellValueDeducer(puzzle, messageBroker).deduceSomeValue().let { deduceResult ->
            if (deduceResult is AnalyzeResult.ValueSet) {
                SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
                deduceResult
            } else {
                AnalyzeResult.combinedResultOf(
                    listOf(
                        CandidateBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates(),
                        CandidateClusterBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates(),
                        StrongLinkUpdater(puzzle, messageBroker).updateStrongLinks(),
                        StrongLinkBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates(),
                        StrongLinkChainBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates(),
                        if (doHeuristicAnalysis) {
                            HeuristicCandidateEliminator(puzzle, messageBroker).eliminateCandidates()
                        } else AnalyzeResult.NoChanges
                    )
                )
            }
        }

    fun updateCandidatesOnly(): AnalyzeResult =
        SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
            .also { result ->
                messageBroker.message(
                    when (result) {
                        AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                        else -> "No changes made"
                    }
                )
            }

    fun updateStrongLinksOnly(): AnalyzeResult =
        StrongLinkUpdater(puzzle, messageBroker).updateStrongLinks()
            .also {
                messageBroker.message("Strong links rebuilt")
            }

    fun eliminateCandidatesOnly(): AnalyzeResult =
        StrongLinkBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()
            .also { result ->
                messageBroker.message(
                    when (result) {
                        AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                        else -> "No changes made"
                    }
                )
            }

    fun deduceValuesOnly(): AnalyzeResult {
        // First update candidates so that deduce doesn't make stupid mistakes.
        SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
        return CellValueDeducer(puzzle, messageBroker).deduceSomeValue()
            .also { result ->
                when (result) {
                    is AnalyzeResult.ValueSet -> {
                        SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
                        messageBroker.message("Value ${result.value} set to cell ${result.coordinates}")
                    }
                    else -> messageBroker.message("No value could be deduced")
                }
            }
    }

    companion object {
        const val DEFAULT_ANALYZE_ROUNDS = 1
    }
}
