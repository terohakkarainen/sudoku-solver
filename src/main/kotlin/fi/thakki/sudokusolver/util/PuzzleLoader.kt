package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.PuzzleMessageBroker
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbols
import org.yaml.snakeyaml.Yaml

object PuzzleLoader {

    fun newPuzzleFromFile(fileName: String, messageBroker: PuzzleMessageBroker): Puzzle =
        this::class.java.classLoader.getResourceAsStream(fileName).use { fileInputStream ->
            Yaml().loadAs(fileInputStream, PuzzleFile::class.java).let { puzzleFile ->
                builderFor(puzzleFile, messageBroker)
                    .apply {
                        puzzleFile.getGivenCells()
                            .forEach { cell -> withGiven(checkNotNull(cell.value), cell.coordinates) }
                    }.build()
            }
        }

    internal fun builderFor(puzzleFile: PuzzleFile, messageBroker: PuzzleMessageBroker): PuzzleBuilder =
        when {
            puzzleFile.hasCustomRegions() -> CustomPuzzleBuilder(puzzleFile, messageBroker)
            else ->
                StandardPuzzleBuilder.StandardLayout.of(puzzleFile.dimension)?.let { standardLayout ->
                    StandardPuzzleBuilder(
                        standardLayout = standardLayout,
                        messageBroker = messageBroker,
                        symbols = Symbols(puzzleFile.symbols)
                    )
                } ?: throw IllegalArgumentException("No standard layout for dimension ${puzzleFile.dimension.value}")
        }
}
