package fi.thakki.sudokusolver.application.canvas

import fi.thakki.sudokusolver.model.Coordinates

data class Rectangle(
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    val topRight: Coordinates
        get() = Coordinates(
            x = bottomRight.x,
            y = topLeft.y
        )

    val bottomLeft: Coordinates
        get() = Coordinates(
            x = topLeft.x,
            y = bottomRight.y
        )
}
