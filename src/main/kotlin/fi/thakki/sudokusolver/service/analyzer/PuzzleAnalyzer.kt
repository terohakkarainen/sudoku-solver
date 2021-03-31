package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.message.PuzzleMessageBroker
import java.time.Duration
import java.time.Instant

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
    ): AnalyzeResult =
        initializePuzzleForAnalyze().let { initializeResult ->
            val startingTime = Instant.now()
            val result = analyzeAtMostRounds(rounds, doHeuristicAnalysis)

            if (result.roundResults.last() == AnalyzeResult.NoChanges) {
                messageBroker.message(
                    "No new results from round ${result.roundResults.size}, " +
                            "stopping analyze after ${milliSecondsSince(startingTime)}ms, " +
                            "${puzzle.readinessPercentage()}% complete."
                )
            } else {
                StrongLinkUpdater(puzzle).updateStrongLinks()
                messageBroker.message(
                    "Analyzed ${result.roundResults.size} round(s), which took ${milliSecondsSince(startingTime)}ms, " +
                            "${puzzle.readinessPercentage()}% complete."
                )
            }

            return AnalyzeResult.combinedResultOf(
                result.roundResults.plus(initializeResult)
            )
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
            when (deduceResult) {
                is AnalyzeResult.ValueSet -> deduceResult
                else -> runEagerly(analyzers(doHeuristicAnalysis))
            }
        }

    private fun analyzers(doHeuristicAnalysis: Boolean): List<AnalyzerFunc> {
        val result = mutableListOf(
            CandidateBasedCandidateEliminator(puzzle, messageBroker)::eliminateCandidates,
            StrongLinkUpdater(puzzle)::updateStrongLinks,
            StrongLinkBasedCandidateEliminator(puzzle, messageBroker)::eliminateCandidates,
            StrongLinkChainBasedCandidateEliminator(puzzle, messageBroker)::eliminateCandidates,
            CandidateClusterBasedCandidateEliminator(puzzle, messageBroker)::eliminateCandidates
        )
        if (doHeuristicAnalysis) {
            result.add(HeuristicCandidateEliminator(puzzle, messageBroker)::eliminateCandidates)
        }
        return result
    }

    fun updateCandidatesOnly(): AnalyzeResult =
        initializePuzzleForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
                        .also { result ->
                            messageBroker.message(
                                when (result) {
                                    AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                                    else -> "No changes made"
                                }
                            )
                        }
                )
            )
        }

    fun updateStrongLinksOnly(): AnalyzeResult =
        initializePuzzleForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    StrongLinkUpdater(puzzle).updateStrongLinks()
                        .also {
                            messageBroker.message("Strong links rebuilt")
                        }
                )
            )
        }

    fun eliminateCandidatesOnly(): AnalyzeResult =
        initializePuzzleForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    StrongLinkBasedCandidateEliminator(puzzle, messageBroker).eliminateCandidates()
                        .also { result ->
                            messageBroker.message(
                                when (result) {
                                    AnalyzeResult.CandidatesEliminated -> "Some candidates removed"
                                    else -> "No changes made"
                                }
                            )
                        }
                )
            )
        }

    fun deduceValuesOnly(): AnalyzeResult =
        initializePuzzleForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    CellValueDeducer(puzzle, messageBroker).deduceSomeValue()
                        .also { result ->
                            when (result) {
                                is AnalyzeResult.ValueSet -> {
                                    // SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
                                    messageBroker.message("Value ${result.value} set to cell ${result.coordinates}")
                                }
                                else -> messageBroker.message("No value could be deduced")
                            }
                        }
                )
            )
        }

    private fun initializePuzzleForAnalyze(): AnalyzeResult =
        if (puzzle.state == Puzzle.State.NOT_ANALYZED_YET) {
            messageBroker.message("Initializing candidates for puzzle")
            SimpleCandidateUpdater(puzzle, messageBroker).updateCandidates()
            puzzle.state = Puzzle.State.ANALYZED
            AnalyzeResult.CandidatesEliminated
        } else AnalyzeResult.NoChanges

    companion object {
        const val DEFAULT_ANALYZE_ROUNDS = 1
    }
}
