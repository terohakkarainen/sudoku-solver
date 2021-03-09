package fi.thakki.sudokusolver

fun main(args: Array<String>) {
    val filename = "expert.yml"
    SudokuSolverConsoleApplication(filename).eventLoop()
}
