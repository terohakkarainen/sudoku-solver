package fi.thakki.sudokusolver.engine.service.mutation

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsOnly
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.engine.model.CellValueType
import fi.thakki.sudokusolver.engine.model.Coordinates
import fi.thakki.sudokusolver.engine.model.Sudoku
import fi.thakki.sudokusolver.engine.model.Symbol
import fi.thakki.sudokusolver.engine.service.SudokuTraverser
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.constraint.CellValueSetException
import fi.thakki.sudokusolver.engine.service.constraint.GivenCellNotModifiableException
import fi.thakki.sudokusolver.engine.service.constraint.SymbolInUseException
import fi.thakki.sudokusolver.engine.service.constraint.SymbolNotSupportedException
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SudokuMutationServiceTest {

    private lateinit var sudoku: Sudoku
    private lateinit var sudokuTraverser: SudokuTraverser
    private lateinit var serviceUnderTest: SudokuMutationService

    private val messageBroker = DiscardingMessageBroker
    private val someCoordinates = Coordinates(0, 0)
    private val otherCoordinatesInSomeRegion = Coordinates(1, 0)
    private val someSymbol: Symbol = '1'
    private val anotherSymbol: Symbol = '2'

    @BeforeEach
    fun setUp() {
        sudoku = StandardSudokuBuilder(StandardSudokuBuilder.StandardLayout.STANDARD_4X4, messageBroker).build()
        sudokuTraverser = SudokuTraverser(sudoku)
        serviceUnderTest = SudokuMutationService(sudoku)
    }

    @Test
    fun `unsupported symbol is rejected upon given`() {
        assertThrows<SymbolNotSupportedException> {
            serviceUnderTest.setCellGiven(someCoordinates, 'w')
        }
    }

    @Test
    fun `cell can be set to given`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        with(sudokuTraverser.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(someSymbol)
            assertThat(type).isEqualTo(CellValueType.GIVEN)
        }
    }

    @Test
    fun `given cell cannot be changed`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<GivenCellNotModifiableException> {
            serviceUnderTest.setCellGiven(someCoordinates, anotherSymbol)
        }
    }

    @Test
    fun `rules apply for given cells`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<SymbolInUseException> {
            serviceUnderTest.setCellGiven(otherCoordinatesInSomeRegion, someSymbol)
        }
    }

    @Test
    fun `unsupported symbol is rejected upon set`() {
        assertThrows<SymbolNotSupportedException> {
            serviceUnderTest.setCellValue(someCoordinates, 'w')
        }
    }

    @Test
    fun `cell can be set to value`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        with(sudokuTraverser.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(someSymbol)
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `cell value can be changed`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        serviceUnderTest.setCellValue(someCoordinates, anotherSymbol)
        with(sudokuTraverser.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(anotherSymbol)
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `given cell cannot be set`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<GivenCellNotModifiableException> {
            serviceUnderTest.setCellValue(someCoordinates, anotherSymbol)
        }
    }

    @Test
    fun `rules apply for set cells`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        assertThrows<SymbolInUseException> {
            serviceUnderTest.setCellValue(otherCoordinatesInSomeRegion, someSymbol)
        }
    }

    @Test
    fun `cell can be reset`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        serviceUnderTest.resetCell(someCoordinates)
        with(sudokuTraverser.cellAt(someCoordinates)) {
            assertThat(value).isNull()
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `blank cell can be reset`() {
        serviceUnderTest.resetCell(someCoordinates)
        with(sudokuTraverser.cellAt(someCoordinates)) {
            assertThat(value).isNull()
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `given cell cannot be reset`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<GivenCellNotModifiableException> {
            serviceUnderTest.resetCell(someCoordinates)
        }
    }

    @Test
    fun `candidate can be toggled`() {
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).contains(someSymbol)

        serviceUnderTest.toggleCandidate(someCoordinates, someSymbol)
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).doesNotContain(someSymbol)

        serviceUnderTest.toggleCandidate(someCoordinates, someSymbol)
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).contains(someSymbol)
    }

    @Test
    fun `candidates can be set`() {
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).hasSize(sudoku.dimension.value)

        serviceUnderTest.setCellCandidates(someCoordinates, setOf(someSymbol))
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).containsOnly(someSymbol)
    }

    @Test
    fun `candidate can be removed`() {
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).contains(someSymbol)

        serviceUnderTest.removeCandidate(someCoordinates, someSymbol)
        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).doesNotContain(someSymbol)
    }

    @Test
    fun `candidate removal does nothing if candidate does not exist`() {
        sudokuTraverser.cellAt(someCoordinates).analysis.candidates = emptySet()

        serviceUnderTest.removeCandidate(someCoordinates, someSymbol)

        assertThat(sudokuTraverser.cellAt(someCoordinates).analysis.candidates).doesNotContain(someSymbol)
    }

    @Test
    fun `candidate removal fails for unsupported symbol`() {
        assertThrows<SymbolNotSupportedException> {
            serviceUnderTest.removeCandidate(someCoordinates, 'w')
        }
    }

    @Test
    fun `candidate removal fails for given cell`() {
        sudokuTraverser.cellAt(someCoordinates).let { cell ->
            cell.type = CellValueType.GIVEN
            cell.value = anotherSymbol
        }

        assertThrows<GivenCellNotModifiableException> {
            serviceUnderTest.removeCandidate(someCoordinates, someSymbol)
        }
    }

    @Test
    fun `candidate removal fails for set cell`() {
        sudokuTraverser.cellAt(someCoordinates).value = anotherSymbol

        assertThrows<CellValueSetException> {
            serviceUnderTest.removeCandidate(someCoordinates, someSymbol)
        }
    }

    @Test
    fun `candidates cannot be set to empty`() {
        assertThrows<IllegalArgumentException> {
            serviceUnderTest.setCellCandidates(someCoordinates, emptySet())
        }
    }
}
