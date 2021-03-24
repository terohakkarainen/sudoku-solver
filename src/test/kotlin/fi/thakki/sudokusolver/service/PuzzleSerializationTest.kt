package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.service.analyzer.PuzzleAnalyzer
import fi.thakki.sudokusolver.util.PuzzleLoader
import fi.thakki.sudokusolver.util.PuzzleTraverser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class PuzzleSerializationTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `Puzzle can be serialized and deserialized without losing data`() {
        val puzzle = PuzzleLoader.newPuzzleFromFile("puzzle.yml", messageBroker)
        val puzzleTraverser = PuzzleTraverser(puzzle)
        PuzzleAnalyzer(puzzle, messageBroker).analyze() // to produce some values, strong links and strong link chains

        val puzzleAsJson = Json.encodeToString(puzzle)
        val restoredPuzzle: Puzzle = Json.decodeFromString(puzzleAsJson)

        assertThat(restoredPuzzle.dimension).isEqualTo(puzzle.dimension)
        assertThat(restoredPuzzle.symbols).containsOnly(*puzzle.symbols.toTypedArray())
        assertThat(restoredPuzzle.analysis.strongLinkChains).isEmpty()

        restoredPuzzle.cells.forEach { restoredCell ->
            puzzleTraverser.cellAt(restoredCell.coordinates).let { originalCell ->
                assertThat(restoredCell.value).isEqualTo(originalCell.value)
                assertThat(restoredCell.type).isEqualTo(originalCell.type)
                assertThat(restoredCell.analysis.candidates).isEqualTo(originalCell.analysis.candidates)
                assertThat(restoredCell.analysis.strongLinks).isEmpty()
            }
        }

        restoredPuzzle.allCellCollections().forEach { collection ->
            assertThat(collection.analysis.strongLinks).isEmpty()
        }

        restoredPuzzle.regions.forEach { restoredRegion ->
            fun cellCoordinatesInRegion(region: Region): Set<Coordinates> =
                region.cells.map { it.coordinates }.toSet()

            val restoredRegionCoordinates = cellCoordinatesInRegion(restoredRegion)
            assertThat(
                restoredRegionCoordinates
            ).isEqualTo(
                cellCoordinatesInRegion(
                    puzzleTraverser.regionOf(
                        puzzleTraverser.cellAt(
                            restoredRegionCoordinates.first()
                        )
                    )
                )
            )
        }
    }
}
