package fi.thakki.sudokusolver.service

object PuzzleMessageBroker {

    fun message(message: String, putLineFeed: Boolean = true) {
        if (putLineFeed) {
            println(message)
        } else print(message)
    }

    fun error(message: String) {
        println(message)
    }
}
