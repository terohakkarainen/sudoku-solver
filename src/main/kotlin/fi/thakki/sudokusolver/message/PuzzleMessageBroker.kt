package fi.thakki.sudokusolver.message

interface PuzzleMessageBroker {
    fun message(message: String)
    fun error(message: String)
}
