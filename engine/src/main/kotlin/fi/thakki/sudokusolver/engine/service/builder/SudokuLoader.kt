package fi.thakki.sudokusolver.engine.service.builder

import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbols
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

object SudokuLoader {

    fun newSudokuFromStream(inputStream: InputStream, messageBroker: SudokuMessageBroker): Sudoku =
        Yaml().loadAs(inputStream, SudokuFile::class.java).let { sudokuFile ->
            builderFor(sudokuFile, messageBroker)
                .apply {
                    sudokuFile.getGivenCells()
                        .forEach { cell -> withGiven(checkNotNull(cell.value), cell.coordinates) }
                }.build()
        }

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
