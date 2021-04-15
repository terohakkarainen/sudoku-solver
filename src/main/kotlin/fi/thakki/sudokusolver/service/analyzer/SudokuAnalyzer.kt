package fi.thakki.sudokusolver.service.analyzer

import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.util.DurationMeasurement

class SudokuAnalyzer(
    private val sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private data class RepeatedAnalyzeResult(
        val completed: Boolean,
        val roundResults: List<AnalyzeResult>
    )

    fun analyze(
        rounds: Int = DEFAULT_ANALYZE_ROUNDS,
        doHeuristicAnalysis: Boolean = true
    ): AnalyzeResult =
        initializeSudokuForAnalyze().let { initializeResult ->
            val durationResult = DurationMeasurement<RepeatedAnalyzeResult>().durationOf {
                analyzeAtMostRounds(rounds, doHeuristicAnalysis)
            }

            val analyzeResult = checkNotNull(durationResult.result)
            if (analyzeResult.roundResults.last() == AnalyzeResult.NoChanges) {
                messageBroker.message(
                    "No new results from round ${analyzeResult.roundResults.size}. " +
                            "Stopping analyze after ${durationResult.duration.toMillis()}ms, sudoku is " +
                            "${sudoku.readinessPercentage()}% complete."
                )
            } else {
                StrongLinkUpdater(sudoku).updateStrongLinks()
                messageBroker.message(
                    "Analyzed ${analyzeResult.roundResults.size} round(s), " +
                            "which took ${durationResult.duration.toMillis()}ms. Sudoku is " +
                            "${sudoku.readinessPercentage()}% complete."
                )
            }
            return AnalyzeResult.combinedResultOf(
                analyzeResult.roundResults.plus(initializeResult)
            )
        }

    private fun analyzeAtMostRounds(maxRounds: Int, doHeuristicAnalysis: Boolean): RepeatedAnalyzeResult {
        var round = 1
        val results = mutableListOf<AnalyzeResult>()

        while (round <= maxRounds && (results.isEmpty() || results.last() != AnalyzeResult.NoChanges)) {
            messageBroker.message("Analyzing sudoku (round $round)...")
            results.add(runAnalyzeRound(doHeuristicAnalysis))
            round++
        }

        return RepeatedAnalyzeResult(
            completed = round == maxRounds,
            roundResults = results
        )
    }

    private fun runAnalyzeRound(doHeuristicAnalysis: Boolean): AnalyzeResult =
        CellValueDeducer(sudoku, messageBroker).deduceSomeValue().let { deduceResult ->
            when (deduceResult) {
                is AnalyzeResult.ValueSet -> deduceResult
                else -> runEagerly(analyzers(doHeuristicAnalysis))
            }
        }

    private fun analyzers(doHeuristicAnalysis: Boolean): List<AnalyzerFunc> {
        val result = mutableListOf(
            CandidateBasedCandidateEliminator(sudoku, messageBroker)::eliminateCandidates,
            StrongLinkUpdater(sudoku)::updateStrongLinks,
            StrongLinkBasedCandidateEliminator(sudoku, messageBroker)::eliminateCandidates,
            StrongLinkChainBasedCandidateEliminator(sudoku, messageBroker)::eliminateCandidates,
            CandidateClusterBasedCandidateEliminator(sudoku, messageBroker)::eliminateCandidates
        )
        if (doHeuristicAnalysis) {
            result.add(HeuristicCandidateEliminator(sudoku, messageBroker)::eliminateCandidates)
        }
        return result
    }

    fun updateCandidatesOnly(): AnalyzeResult =
        initializeSudokuForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    SimpleCandidateUpdater(sudoku, messageBroker).updateCandidates()
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
        initializeSudokuForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    StrongLinkUpdater(sudoku).updateStrongLinks()
                        .also {
                            messageBroker.message("Strong links rebuilt")
                        }
                )
            )
        }

    fun eliminateCandidatesOnly(): AnalyzeResult =
        initializeSudokuForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    StrongLinkBasedCandidateEliminator(sudoku, messageBroker).eliminateCandidates()
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
        initializeSudokuForAnalyze().let { initializeResult ->
            AnalyzeResult.combinedResultOf(
                listOf(
                    initializeResult,
                    CellValueDeducer(sudoku, messageBroker).deduceSomeValue()
                        .also { result ->
                            when (result) {
                                is AnalyzeResult.ValueSet ->
                                    messageBroker.message("Value ${result.value} set to cell ${result.coordinates}")
                                else -> messageBroker.message("No value could be deduced")
                            }
                        }
                )
            )
        }

    private fun initializeSudokuForAnalyze(): AnalyzeResult =
        if (sudoku.state == Sudoku.State.NOT_ANALYZED_YET) {
            messageBroker.message("Initializing candidates for sudoku")
            SimpleCandidateUpdater(sudoku, messageBroker).updateCandidates()
            sudoku.state = Sudoku.State.ANALYZED
            AnalyzeResult.CandidatesEliminated
        } else AnalyzeResult.NoChanges

    companion object {
        const val DEFAULT_ANALYZE_ROUNDS = 1
    }
}
