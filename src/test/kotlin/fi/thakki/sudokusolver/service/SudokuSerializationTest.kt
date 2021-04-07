package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import fi.thakki.sudokusolver.message.ConsoleApplicationMessageBroker
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Region
import fi.thakki.sudokusolver.service.analyzer.SudokuAnalyzer
import fi.thakki.sudokusolver.util.SudokuLoader
import fi.thakki.sudokusolver.util.SudokuTraverser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class SudokuSerializationTest {

    private val messageBroker = ConsoleApplicationMessageBroker

    @Test
    fun `Sudoku can be serialized and deserialized without losing data`() {
        val sudoku = SudokuLoader.newSudokuFromFile("sudoku.yml", messageBroker)
        val sudokuTraverser = SudokuTraverser(sudoku)
        SudokuAnalyzer(sudoku, messageBroker).analyze() // to produce some values, strong links and strong link chains

        val sudokuAsJson = Json.encodeToString(sudoku)
        val restoredSudoku: Sudoku = Json.decodeFromString(sudokuAsJson)

        assertThat(restoredSudoku.dimension).isEqualTo(sudoku.dimension)
        assertThat(restoredSudoku.symbols).containsOnly(*sudoku.symbols.toTypedArray())
        assertThat(restoredSudoku.analysis.strongLinkChains).isEmpty()

        restoredSudoku.cells.forEach { restoredCell ->
            sudokuTraverser.cellAt(restoredCell.coordinates).let { originalCell ->
                assertThat(restoredCell.value).isEqualTo(originalCell.value)
                assertThat(restoredCell.type).isEqualTo(originalCell.type)
                assertThat(restoredCell.analysis.candidates).isEqualTo(originalCell.analysis.candidates)
                assertThat(restoredCell.analysis.strongLinks).isEmpty()
            }
        }

        restoredSudoku.allCellCollections().forEach { collection ->
            assertThat(collection.analysis.strongLinks).isEmpty()
        }

        restoredSudoku.regions.forEach { restoredRegion ->
            fun cellCoordinatesInRegion(region: Region): Set<Coordinates> =
                region.cells.map { it.coordinates }.toSet()

            val restoredRegionCoordinates = cellCoordinatesInRegion(restoredRegion)
            assertThat(
                restoredRegionCoordinates
            ).isEqualTo(
                cellCoordinatesInRegion(
                    sudokuTraverser.regionOf(
                        sudokuTraverser.cellAt(
                            restoredRegionCoordinates.first()
                        )
                    )
                )
            )
        }
    }
}
