package fi.thakki.sudokusolver.service.mutation

import fi.thakki.sudokusolver.model.RevisionInformation
import fi.thakki.sudokusolver.model.Sudoku
import java.time.ZonedDateTime

class SudokuRevisionService {

    abstract class SudokuRevisionException(message: String) : RuntimeException(message)

    class PreviousRevisionDoesNotExistException :
        SudokuRevisionException("There is no previous revision, sudoku at initial revision")

    class NoRevisionsRecordedException :
        SudokuRevisionException("No revision has been recorded yet")

    data class SudokuRevision(
        val number: Int,
        val createdAt: ZonedDateTime,
        val description: String,
        val sudoku: Sudoku
    )

    private data class PersistedSudokuRevision(
        val number: Int,
        val createdAt: ZonedDateTime,
        val description: String,
        val data: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PersistedSudokuRevision
            return number == other.number
        }

        override fun hashCode(): Int = number
    }

    private val revisions = mutableListOf<PersistedSudokuRevision>()

    fun newRevision(sudoku: Sudoku, description: String): RevisionInformation =
        PersistedSudokuRevision(
            number = latestRevision()?.inc() ?: INITIAL_REVISION,
            createdAt = ZonedDateTime.now(),
            description = description,
            data = SudokuSerializationService.serialize(sudoku)
        ).let { persistedRevision ->
            revisions.add(persistedRevision)
            toRevisionInformation(persistedRevision)
        }

    fun restorePreviousRevision(): SudokuRevision =
        latestRevision()?.let { latestRevision ->
            if (latestRevision == INITIAL_REVISION) {
                throw PreviousRevisionDoesNotExistException()
            }
            revisions.removeLast()
            toSudokuRevision(revisions.last())
        } ?: throw NoRevisionsRecordedException()

    fun restoreRevision(number: Int): SudokuRevision {
        while (true) {
            restorePreviousRevision().let { restored ->
                if (restored.number == number) {
                    return restored
                }
            }
        }
    }

    private fun latestRevision(): Int? =
        when {
            revisions.isEmpty() -> null
            else -> revisions.last().number
        }

    private fun toSudokuRevision(persistedRevision: PersistedSudokuRevision) =
        SudokuRevision(
            number = persistedRevision.number,
            createdAt = persistedRevision.createdAt,
            description = persistedRevision.description,
            sudoku = SudokuSerializationService.deserialize(persistedRevision.data).apply {
                revisionInformation = toRevisionInformation(persistedRevision)
            }
        )

    private fun toRevisionInformation(persistedRevision: PersistedSudokuRevision) =
        RevisionInformation(
            number = persistedRevision.number,
            createdAt = persistedRevision.createdAt,
            description = persistedRevision.description
        )

    companion object {
        internal const val INITIAL_REVISION = 1
    }
}
