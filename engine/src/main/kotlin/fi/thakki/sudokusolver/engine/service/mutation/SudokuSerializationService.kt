package fi.thakki.sudokusolver.engine.service.mutation

import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.util.StringCompressor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SudokuSerializationService {

    fun serialize(sudoku: Sudoku): ByteArray =
        StringCompressor.compress(Json.encodeToString(sudoku))

    fun deserialize(sudokuData: ByteArray): Sudoku =
        Json.decodeFromString(
            StringCompressor.decompress(sudokuData)
        )

    fun copyOf(sudoku: Sudoku): Sudoku =
        Json.encodeToString(sudoku).let { json ->
            Json.decodeFromString(json)
        }
}
