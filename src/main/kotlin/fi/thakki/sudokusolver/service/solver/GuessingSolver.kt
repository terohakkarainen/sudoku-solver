package fi.thakki.sudokusolver.service.solver

import fi.thakki.sudokusolver.service.message.DiscardingMessageBroker
import fi.thakki.sudokusolver.service.message.SudokuMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.RevisionInformation
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.service.constraint.SudokuConstraintViolationException
import fi.thakki.sudokusolver.service.mutation.SudokuMutationService
import fi.thakki.sudokusolver.service.mutation.SudokuRevisionService
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer
import kotlin.math.roundToInt

typealias ProgressListener = (Int) -> Unit

@Suppress("TooManyFunctions")
class GuessingSolver(
    private var sudoku: Sudoku,
    private val messageBroker: SudokuMessageBroker
) {

    private data class GuessTarget(
        val coordinates: Coordinates,
        val candidate: Symbol
    )

    private var currentDepth = 0
    private var cycles = 0L
    private var isSolved = false
    private var progressPercentage = 0

    private val revisionService = SudokuRevisionService().apply {
        newRevision(sudoku, "Initial revision").also { revisionInfo ->
            setCurrentRevisionInfo(revisionInfo)
        }
    }

    fun solve(): Sudoku? {
        prepareSudokuForGuessing()
        guess { currentProgress -> progressPercentage = currentProgress }
        return if (isSolved) sudoku else null
    }

    private fun prepareSudokuForGuessing() {
        silentAnalyzerOf(sudoku).let { analyzer ->
            if (sudoku.state == Sudoku.State.NOT_ANALYZED_YET) {
                analyzer.updateCandidatesOnly()
                sudoku.state = Sudoku.State.ANALYZED
            }
            analyzer.updateStrongLinksOnly()
        }
    }

    private fun guess(progressListener: ProgressListener? = null) {
        if (currentDepth < MAX_DEPTH) {
            currentDepth += 1
            printProgress()
            val targets = getGuessTargets()
            targets.forEachIndexed { index, target ->
                val revisionBefore = currentRevisionNumber()
                guessCell(target)
                if (isSolved) return
                val revisionAfter = currentRevisionNumber()
                if (revisionBefore != revisionAfter) {
                    sudoku = restoreRevision(revisionBefore)
                    require(currentRevisionNumber() == revisionBefore)
                }
                progressListener?.let { listener ->
                    val progressPercentage =
                        ((index.toDouble() / targets.size.toDouble()) * HUNDRED_PERCENT).roundToInt()
                    listener(progressPercentage)
                }
            }
            currentDepth -= 1
        }
    }

    private fun getGuessTargets(): Set<GuessTarget> =
        sudoku.allCellCollections()
            .flatMap { it.analysis.strongLinks }
            .toSet()
            .groupBy { it.symbol }
            .let { strongLinksBySymbol ->
                strongLinksBySymbol.keys
                    .flatMap { symbol ->
                        checkNotNull(strongLinksBySymbol[symbol])
                            .flatMap { it.cells() }
                            .map { cell -> GuessTarget(cell.coordinates, symbol) }
                    }.shuffled().toSet()
            }

    private fun guessCell(target: GuessTarget) {
        val oldRevision = currentRevisionNumber()
        if (candidateRemovalLeavesSudokuIntact(target)) {
            require(currentRevisionNumber() != oldRevision)
            // create a new revision so that we can return to this point as safe.
            storeNewRevision()
            if (sudoku.state == Sudoku.State.COMPLETE) isSolved = true else guess()
        }
    }

    private fun candidateRemovalLeavesSudokuIntact(target: GuessTarget): Boolean =
        try {
            cycles += 1
            // create a new revision so that we can back off if constraint gets violated.
            storeNewRevision()
            SudokuMutationService(sudoku).toggleCandidate(target.coordinates, target.candidate)
            silentAnalyzerOf(sudoku).analyze(rounds = Int.MAX_VALUE, doHeuristicAnalysis = false)
            true
        } catch (e: SudokuConstraintViolationException) {
            sudoku = restoreRevision()
            false
        }

    private fun storeNewRevision() {
        setCurrentRevisionInfo(
            revisionService.newRevision(sudoku, "Guess at level $currentDepth")
        )
    }

    private fun setCurrentRevisionInfo(revisionInfo: RevisionInformation) {
        sudoku.revisionInformation = revisionInfo
    }

    private fun restoreRevision(revisionNumber: Int? = null): Sudoku {
        val result = revisionNumber?.let { revisionService.restoreRevision(it).sudoku }
            ?: revisionService.restorePreviousRevision().sudoku
        silentAnalyzerOf(result).updateStrongLinksOnly()
        return result
    }

    private fun currentRevisionNumber() =
        checkNotNull(sudoku.revisionInformation).number

    private fun printProgress() {
        messageBroker.message("*".repeat(currentDepth * 2) + " ($currentDepth, $cycles, $progressPercentage%)")
    }

    private fun silentAnalyzerOf(sudoku: Sudoku): SudokuAnalyzer =
        SudokuAnalyzer(sudoku, DiscardingMessageBroker)

    companion object {
        // Tests have shown that depth of 3 is sufficient for breaking even the most hardest sudokus.
        const val MAX_DEPTH = 3
        const val HUNDRED_PERCENT = 100f
    }
}
