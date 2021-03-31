package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates

class Painter(private val layer: Layer) {

    var painterFgColor: Color = Color.DEFAULT

    fun pixel(
        character: String,
        coordinates: Coordinates,
        fgColor: Color? = null,
        bgColor: Color? = null
    ) {
        with(layer.pixelAt(coordinates)) {
            this.character = character
            this.bgColor = bgColor
            this.fgColor = fgColor ?: painterFgColor
        }
    }

    fun rectangle(
        bottomLeft: Coordinates,
        topRight: Coordinates,
        bgColor: Color
    ) {
        layer.pixelsIn { coordinates ->
            coordinates.x >= bottomLeft.x && coordinates.x <= topRight.x &&
                    coordinates.y >= bottomLeft.y && coordinates.y <= topRight.y
        }.forEach { affectedPixel ->
            affectedPixel.bgColor = bgColor
        }
    }

    fun line(
        from: Coordinates,
        to: Coordinates,
        character: String,
        fgColor: Color? = null
    ) {
        require(from.x == to.x || from.y == to.y) { "Can only paint horizontal or vertical lines" }
        layer.pixelsIn { coordinates ->
            coordinates.x >= from.x && coordinates.x <= to.x && coordinates.y >= from.y && coordinates.y <= to.y
        }.forEach { affectedPixel ->
            affectedPixel.character = character
            affectedPixel.fgColor = fgColor ?: painterFgColor
        }
    }
}
