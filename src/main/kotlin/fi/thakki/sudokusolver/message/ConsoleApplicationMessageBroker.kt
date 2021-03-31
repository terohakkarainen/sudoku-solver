package fi.thakki.sudokusolver.message

object ConsoleApplicationMessageBroker : PuzzleMessageBroker {

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
