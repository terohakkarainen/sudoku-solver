package fi.thakki.sudokusolver.engine.service.message

interface SudokuMessageBroker {
    fun message(message: String)
    fun error(message: String)
}
