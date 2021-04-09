package fi.thakki.sudokusolver.service

import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.util.StringCompressor
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
