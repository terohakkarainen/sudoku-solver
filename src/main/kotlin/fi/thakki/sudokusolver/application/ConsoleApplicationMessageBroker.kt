package fi.thakki.sudokusolver.application

import fi.thakki.sudokusolver.service.message.SudokuMessageBroker

object ConsoleApplicationMessageBroker : SudokuMessageBroker {

    override fun message(message: String) {
        message(message, true)
    }

    fun message(message: String, putLineFeed: Boolean) {
        if (putLineFeed) {
            println(message)
        } else print(message)
    }

    override fun error(message: String) {
        println(message)
    }
}
