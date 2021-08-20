package fi.thakki.sudokusolver.webapp

import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.message.SudokuMessageBroker
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {

    val messageBroker = object : SudokuMessageBroker {
        override fun message(message: String) {
            println("message from engine: $message")
        }

        override fun error(message: String) {
            println("error from engine: $message")
        }
    }

    val sudoku = StandardSudokuBuilder(
        standardLayout = StandardSudokuBuilder.StandardLayout.STANDARD_9X9,
        messageBroker = messageBroker
    ).build()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting(sudoku)
    }.start(wait = true)
}
