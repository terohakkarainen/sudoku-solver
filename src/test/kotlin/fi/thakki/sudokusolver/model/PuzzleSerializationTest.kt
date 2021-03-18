package fi.thakki.sudokusolver.model

import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer
import fi.thakki.sudokusolver.service.analyzer.StrongLinkUpdater
import fi.thakki.sudokusolver.util.PuzzleLoader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class PuzzleSerializationTest {

    @Test
    @Disabled
    fun foo() {
        val puzzle = PuzzleLoader.newPuzzleFromFile("puzzle.yml")
        PuzzleAnalyzer(puzzle).analyze()
        StrongLinkUpdater(puzzle).resetAllStrongLinks()
//        puzzle.cells.cellsWithValue().forEach { cell -> cell.analysis.strongLinks = emptySet() }
        val json = Json.encodeToString(puzzle)
        println(json)

        val restoredPuzzle: Puzzle = Json.decodeFromString(json)
        println(restoredPuzzle)
//        assertThat(restoredPuzzle).isEqualTo(puzzle)
    }
}
