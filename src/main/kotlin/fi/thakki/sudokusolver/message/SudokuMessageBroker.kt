package fi.thakki.sudokusolver.message

interface SudokuMessageBroker {
    fun message(message: String)
    fun error(message: String)
}
