package fi.thakki.sudokusolver

interface PuzzleMessageBroker {

    fun message(message: String)
    fun error(message: String)
}
