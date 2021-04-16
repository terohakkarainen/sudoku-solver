package fi.thakki.sudokusolver.service.command

import fi.thakki.sudokusolver.model.Sudoku

data class CommandOutcome(
    val sudokuModified: Boolean,
    val resultingSudoku: Sudoku? = null
) {
    companion object {
        val sudokuNotModified = CommandOutcome(false)
        val sudokuModified = CommandOutcome(true)
    }
}
