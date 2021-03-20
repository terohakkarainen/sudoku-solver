package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Puzzle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

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
        val data: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PersistedPuzzleRevision
            return revision == other.revision
        }

        override fun hashCode(): Int = revision
    }

    private val revisions = mutableListOf<PersistedPuzzleRevision>()

    fun newRevision(puzzle: Puzzle): String {
        val revision = latestRevision()?.inc() ?: INITIAL_REVISION
        revisions.add(
            PersistedPuzzleRevision(revision, compress(Json.encodeToString(puzzle)))
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
                    Json.decodeFromString(decompress(previousRevision.data))
                )
            }
        } ?: throw NoRevisionsRecordedException()

    private fun latestRevision(): Int? =
        when {
            revisions.isEmpty() -> null
            else -> revisions.last().revision
        }

    private fun compress(s: String): ByteArray =
        ByteArrayOutputStream().use { bos ->
            GZIPOutputStream(bos).use { gos ->
                gos.bufferedWriter(UTF_8).use { it.write(s) }
            }
            bos.toByteArray()
        }

    private fun decompress(data: ByteArray): String =
        GZIPInputStream(data.inputStream()).use { gis ->
            gis.bufferedReader(UTF_8).use { it.readText() }
        }
}
