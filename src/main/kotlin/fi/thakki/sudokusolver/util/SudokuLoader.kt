package fi.thakki.sudokusolver.util

import fi.thakki.sudokusolver.message.SudokuMessageBroker
import fi.thakki.sudokusolver.model.Sudoku
import fi.thakki.sudokusolver.model.Symbols
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

object SudokuLoader {

    fun newSudokuFromStream(inputStream: InputStream, messageBroker: SudokuMessageBroker): Sudoku =
//        this::class.java.classLoader.getResourceAsStream(fileName).use { fileInputStream ->
        Yaml().loadAs(inputStream, SudokuFile::class.java).let { sudokuFile ->
            builderFor(sudokuFile, messageBroker)
                .apply {
                    sudokuFile.getGivenCells()
                        .forEach { cell -> withGiven(checkNotNull(cell.value), cell.coordinates) }
                }.build()
        }
//        }

    internal fun builderFor(sudokuFile: SudokuFile, messageBroker: SudokuMessageBroker): SudokuBuilder =
        when {
            sudokuFile.hasCustomRegions() -> CustomSudokuBuilder(sudokuFile, messageBroker)
            else ->
                StandardSudokuBuilder.StandardLayout.of(sudokuFile.dimension)?.let { standardLayout ->
                    StandardSudokuBuilder(
                        standardLayout = standardLayout,
                        messageBroker = messageBroker,
                        symbols = Symbols(sudokuFile.symbols)
                    )
                } ?: throw IllegalArgumentException("No standard layout for dimension ${sudokuFile.dimension.value}")
        }
}
