package fi.thakki.sudokusolver.message

object DiscardingMessageBroker : SudokuMessageBroker {

    override fun message(message: String) {
        // Nop.
    }

    override fun error(message: String) {
        // Nop.
    }
}
