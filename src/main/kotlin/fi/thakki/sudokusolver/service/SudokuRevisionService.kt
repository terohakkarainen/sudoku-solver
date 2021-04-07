package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Sudoku
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

object SudokuRevisionService {

    private const val INITIAL_REVISION = 1

    abstract class SudokuRevisionException(message: String) : RuntimeException(message)

    class PreviousRevisionDoesNotExistException :
        SudokuRevisionException("There is no previous revision, sudoku at initial revision")

    class NoRevisionsRecordedException :
        SudokuRevisionException("No revision has been recorded yet")

    data class SudokuRevision(
        val description: String,
        val sudoku: Sudoku
    )

    private data class PersistedSudokuRevision(
        val revision: Int,
        val data: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PersistedSudokuRevision
            return revision == other.revision
        }

        override fun hashCode(): Int = revision
    }

    private val revisions = mutableListOf<PersistedSudokuRevision>()

    fun newRevision(sudoku: Sudoku): String {
        val revision = latestRevision()?.inc() ?: INITIAL_REVISION
        revisions.add(
            PersistedSudokuRevision(revision, compress(Json.encodeToString(sudoku)))
        )
        return revision.toString()
    }

    fun previousRevision(): SudokuRevision =
        latestRevision()?.let { latestRevision ->
            if (latestRevision == INITIAL_REVISION) {
                throw PreviousRevisionDoesNotExistException()
            }
            revisions.removeLast()
            revisions.last().let { previousRevision ->
                SudokuRevision(
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

    fun copyOf(sudoku: Sudoku): Sudoku =
        Json.encodeToString(sudoku).let { json ->
            Json.decodeFromString(json)
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
