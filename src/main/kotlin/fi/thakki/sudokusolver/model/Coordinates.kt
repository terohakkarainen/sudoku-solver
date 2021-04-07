package fi.thakki.sudokusolver.model

import kotlinx.serialization.Serializable

typealias Coordinate = Int

@Serializable
data class Coordinates(val x: Coordinate, val y: Coordinate) {

    init {
        require(x >= 0) { "x coordinate must be greater or equal to than zero" }
        require(y >= 0) { "y coordinate must be greater or equal to than zero" }
    }

    override fun toString(): String = "($x,$y)"
}
