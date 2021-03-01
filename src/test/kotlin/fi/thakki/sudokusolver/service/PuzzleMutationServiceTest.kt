package fi.thakki.sudokusolver.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fi.thakki.sudokusolver.model.CellValueType
import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Puzzle
import fi.thakki.sudokusolver.model.Symbol
import fi.thakki.sudokusolver.util.PuzzleBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PuzzleMutationServiceTest {

    private lateinit var puzzle: Puzzle
    private lateinit var serviceUnderTest: PuzzleMutationService

    private val someCoordinates = Coordinates(0, 0)
    private val otherCoordinatesInSomeRegion = Coordinates(1, 0)
    private val someSymbol: Symbol = "1"
    private val anotherSymbol: Symbol = "2"

    @BeforeEach
    fun setUp() {
        puzzle = PuzzleBuilder(layout = PuzzleBuilder.Layout.STANDARD_4X4).build()
        serviceUnderTest = PuzzleMutationService(puzzle)
    }

    @Test
    fun `unsupported symbol is rejected upon given`() {
        assertThrows<PuzzleMutationService.SymbolNotSupportedException> {
            serviceUnderTest.setCellGiven(someCoordinates, "foo")
        }
    }

    @Test
    fun `cell can be set to given`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        with(puzzle.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(someSymbol)
            assertThat(type).isEqualTo(CellValueType.GIVEN)
        }
    }

    @Test
    fun `given cell cannot be changed`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<PuzzleMutationService.CellGivenException> {
            serviceUnderTest.setCellGiven(someCoordinates, anotherSymbol)
        }
    }

    @Test
    fun `rules apply for given cells`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<PuzzleMutationService.SymbolInUseException> {
            serviceUnderTest.setCellGiven(otherCoordinatesInSomeRegion, someSymbol)
        }
    }

    @Test
    fun `unsupported symbol is rejected upon set`() {
        assertThrows<PuzzleMutationService.SymbolNotSupportedException> {
            serviceUnderTest.setCellValue(someCoordinates, "foo")
        }
    }

    @Test
    fun `cell can be set to value`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        with(puzzle.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(someSymbol)
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `cell value can be changed`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        serviceUnderTest.setCellValue(someCoordinates, anotherSymbol)
        with(puzzle.cellAt(someCoordinates)) {
            assertThat(value).isEqualTo(anotherSymbol)
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `given cell cannot be set`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<PuzzleMutationService.CellGivenException> {
            serviceUnderTest.setCellValue(someCoordinates, anotherSymbol)
        }
    }

    @Test
    fun `rules apply for set cells`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        assertThrows<PuzzleMutationService.SymbolInUseException> {
            serviceUnderTest.setCellValue(otherCoordinatesInSomeRegion, someSymbol)
        }
    }

    @Test
    fun `cell can be reset`() {
        serviceUnderTest.setCellValue(someCoordinates, someSymbol)
        serviceUnderTest.resetCell(someCoordinates)
        with(puzzle.cellAt(someCoordinates)) {
            assertThat(value).isNull()
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `blank cell can be reset`() {
        serviceUnderTest.resetCell(someCoordinates)
        with(puzzle.cellAt(someCoordinates)) {
            assertThat(value).isNull()
            assertThat(type).isEqualTo(CellValueType.SETTABLE)
        }
    }

    @Test
    fun `given cell cannot be reset`() {
        serviceUnderTest.setCellGiven(someCoordinates, someSymbol)
        assertThrows<PuzzleMutationService.CellGivenException> {
            serviceUnderTest.resetCell(someCoordinates)
        }
    }
}
