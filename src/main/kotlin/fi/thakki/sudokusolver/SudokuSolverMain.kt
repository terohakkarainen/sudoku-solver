package fi.thakki.sudokusolver

import fi.thakki.sudokusolver.application.SudokuSolverConsoleApplication
import kotlin.system.exitProcess

object SudokuSolverMain {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            println("SudokuSolver usage: java -jar <application-jar-file> <input-yml-file>")
            exitProcess(1)
        } else {
            SudokuSolverConsoleApplication(args.single()).eventLoop()
            exitProcess(0)
        }
    }
}
