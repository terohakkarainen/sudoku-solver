package fi.thakki.sudokusolver.model

data class Size(
    val width: Int,
    val height: Int
) {
    init {
        require(width >= 0) { "Width must be greater or equal to than zero" }
        require(height >= 0) { "Height must be greater or equal to than zero" }
    }

    override fun toString(): String = "($width,$height)"
}
