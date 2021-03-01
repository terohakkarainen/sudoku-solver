package fi.thakki.sudokusolver.model

data class Coordinates(val x: Int, val y: Int) {

    init {
        require(x >= 0) {
            "x coordinate must be greater or equal to than zero"
        }
        require(y >= 0) {
            "y coordinate must be greater or equal to than zero"
        }
    }

    override fun toString(): String = "($x,$y)"
}
