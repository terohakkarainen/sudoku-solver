package fi.thakki.sudokusolver.engine.service.analyzer

import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Symbol

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
                results.any { it == CandidatesEliminated } -> CandidatesEliminated
                else -> NoChanges
            }
    }
}
