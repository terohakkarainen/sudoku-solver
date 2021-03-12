package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleBuilder
import fi.thakki.sudokusolver.util.PuzzleTraverser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleMutationServiceTest {

    private lateinit var puzzle: Puzzle
    private lateinit var puzzleTraverser: PuzzleTraverser
    private lateinit var serviceUnderTest: PuzzleMutationService

    private val someCoordinates = Coordinates(0, 0)
    private val otherCoordinatesInSomeRegion = Coordinates(1, 0)
    private val someSymbol: Symbol = '1'
    private val anotherSymbol: Symbol = '2'

    @BeforeEach
    fun setUp() {
        puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_4X4).build()
        puzzleTraverser = PuzzleTraverser(puzzle)
        serviceUnderTest = PuzzleMutationService(puzzle)
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
        with(puzzleTraverser.cellAt(someCoordinates)) {
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
        with(puzzleTraverser.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(someSymbol)
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `cell value can be changed`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        serviceUnderTest.setCellValue(someCoordinates, anotherSymbol)
        with(puzzleTraverser.cellAt(someCoordinates)) {
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
        with(puzzleTraverser.cellAt(someCoordinates)) {
            assertThat(value).isNull()
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `blank cell can be reset`() {
        serviceUnderTest.resetCell(someCoordinates)
        with(puzzleTraverser.cellAt(someCoordinates)) {
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
}
