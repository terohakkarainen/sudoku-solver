package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates

class Painter(private val layer: Layer) {

    var painterFgColor: Color = Color.DEFAULT

    fun draw(
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

    // TODO needed?
    fun fill(bgColor: Color) {
        layer.pixels().forEach { pixel ->
            pixel.bgColor = bgColor
        }
    }

    fun line(
        from: Coordinates,
        to: Coordinates,
        character: String
    ) {
        require(from.x == to.x || from.y == to.y) { "Can only paint horizontal or vertical lines" }
        layer.pixelsIn { coordinates ->
            coordinates.x >= from.x && coordinates.x <= to.x && coordinates.y >= from.y && coordinates.y <= to.y
        }.forEach { affectedPixel ->
            affectedPixel.character = character
        }
    }
}
