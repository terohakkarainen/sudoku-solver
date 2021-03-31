package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.application.SudokuSolverConsoleApplication

fun main(args: Array<String>) {
    val filename = "expert2.yml"
    SudokuSolverConsoleApplication(filename).eventLoop()
}
