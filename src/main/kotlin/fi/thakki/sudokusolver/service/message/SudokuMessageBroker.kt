package fi.thakki.sudokusolver.service.message

interface SudokuMessageBroker {
    fun message(message: String)
    fun error(message: String)
}
