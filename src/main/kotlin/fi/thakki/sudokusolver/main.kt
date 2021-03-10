package fi.thakki.sudokusolver

fun main(args: Array<String>) {
    val filename = "expert2.yml"
    SudokuSolverConsoleApplication(filename).eventLoop()
}
