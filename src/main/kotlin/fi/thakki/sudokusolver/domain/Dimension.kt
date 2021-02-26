package fi.thakki.sudokusolver.domain

class Dimension(val value: Int) {

    init {
        require(value > 0) {
            "Dimension must be greater than zero"
        }
    }
}
