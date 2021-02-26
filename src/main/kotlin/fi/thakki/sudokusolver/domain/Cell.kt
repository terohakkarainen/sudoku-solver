package fi.thakki.sudokusolver.domain

enum class CellValueType {
    SETTABLE,
    GIVEN
}

class Cell(
    val coordinates: Coordinates,
    var value: Symbol? = null,
    private var type: CellValueType = CellValueType.SETTABLE
) {

    fun setGiven(value: Symbol) {
        this.value = value
        type = CellValueType.GIVEN
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Cell
        return coordinates == other.coordinates
    }

    override fun hashCode(): Int = coordinates.hashCode()
}
