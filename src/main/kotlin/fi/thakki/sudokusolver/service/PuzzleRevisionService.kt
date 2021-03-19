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

    private val revisions = mutableListOf<PuzzleRevision>()

    data class PoppedPuzzleRevision(
        val description: String,
        val puzzle: Puzzle
    )

    data class PuzzleRevision(
        val revision: Int,
        val data: String
    )

    fun newRevision(puzzle: Puzzle): String {
        val revision = latestRevision()?.inc() ?: INITIAL_REVISION
        revisions.add(PuzzleRevision(revision, Json.encodeToString(puzzle)))
        return revision.toString()
    }

    fun previousRevision(): PoppedPuzzleRevision =
        latestRevision()?.let { latestRevision ->
            if (latestRevision == INITIAL_REVISION) {
                throw PreviousRevisionDoesNotExistException()
            }
            revisions.removeAt(revisions.size - 1)
            revisions.last().let { previousRevision ->
                PoppedPuzzleRevision(
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
