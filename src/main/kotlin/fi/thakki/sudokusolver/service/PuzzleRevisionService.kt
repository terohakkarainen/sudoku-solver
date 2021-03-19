package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Puzzle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO add data gzipping
object PuzzleRevisionService {

    private const val INITIAL_REVISION = 1

    abstract class PuzzleRevisionException(message: String) : RuntimeException(message)

    class PreviousRevisionDoesNotExistException :
        PuzzleRevisionException("There is no previous revision, puzzle at initial revision")

    class NoRevisionsRecordedException :
        PuzzleRevisionException("No revision has been recorded yet")

    data class PuzzleRevision(
        val description: String,
        val puzzle: Puzzle
    )

    private data class PersistedPuzzleRevision(
        val revision: Int,
        val data: String
    )

    private val revisions = mutableListOf<PersistedPuzzleRevision>()

    fun newRevision(puzzle: Puzzle): String {
        val revision = latestRevision()?.inc() ?: INITIAL_REVISION
        revisions.add(
            PersistedPuzzleRevision(revision, Json.encodeToString(puzzle))
        )
        return revision.toString()
    }

    fun previousRevision(): PuzzleRevision =
        latestRevision()?.let { latestRevision ->
            if (latestRevision == INITIAL_REVISION) {
                throw PreviousRevisionDoesNotExistException()
            }
            revisions.removeLast()
            revisions.last().let { previousRevision ->
                PuzzleRevision(
                    previousRevision.revision.toString(),
                    Json.decodeFromString(previousRevision.data)
                )
            }
        } ?: throw NoRevisionsRecordedException()

    private fun latestRevision(): Int? =
        when {
            revisions.isEmpty() -> null
            else -> revisions.last().revision
        }
}
