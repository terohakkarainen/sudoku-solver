package fi.thakki.sudokusolver.engine.model

import kotlinx.serialization.Serializable

@Serializable
class Dimension(val value: Int) {

    init {
        require(value > 0) { "Dimension must be greater than zero" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Dimension
        return value == other.value
    }

    override fun hashCode(): Int = value
}
