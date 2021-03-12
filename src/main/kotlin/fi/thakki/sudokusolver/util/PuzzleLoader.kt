package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbols
import org.yaml.snakeyaml.Yaml

object PuzzleLoader {

    fun newPuzzleFromFile(fileName: String): Puzzle =
        this::class.java.classLoader.getResourceAsStream(fileName).use { fileInputStream ->
            Yaml().loadAs(fileInputStream, PuzzleFile::class.java).let { puzzleFile ->
                PuzzleBuilder(
                    layout = PuzzleBuilder.Layout.of(puzzleFile.dimension),
                    symbols = Symbols(puzzleFile.symbols)
                ).apply {
                    puzzleFile.getGivenCells().forEach { cell -> withGiven(checkNotNull(cell.value), cell.coordinates) }
                }.build()
            }
        }
}
