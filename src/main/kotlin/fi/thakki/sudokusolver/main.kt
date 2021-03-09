package fi.thakki.sudokusolver

fun main(args: Array<String>) {
    val filename = "puzzle.yml"
    SudokuSolverConsoleApplication(filename).eventLoop()
}
