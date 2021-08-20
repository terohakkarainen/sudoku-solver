package fi.thakki.sudokusolver.webapp

import fi.thakki.sudokusolver.engine.model.Sudoku
import io.ktor.routing.routing
import io.ktor.routing.get
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import java.time.LocalDateTime

fun Application.configureRouting(sudoku: Sudoku) {
    routing {
        get("/") {
            call.respondText("${LocalDateTime.now()}: SudokuSolver in state ${sudoku.state.name}")
        }
    }
}
