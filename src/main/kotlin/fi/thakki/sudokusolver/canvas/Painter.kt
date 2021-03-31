package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates
import kotlin.math.roundToInt

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

    fun textArea(
        bottomLeft: Coordinates,
        topRight: Coordinates,
        character: String
    ) {
        layer.pixelsIn { coordinates ->
            coordinates.x >= bottomLeft.x && coordinates.x <= topRight.x &&
                    coordinates.y >= bottomLeft.y && coordinates.y <= topRight.y
        }.forEach { affectedPixel ->
            affectedPixel.character = character
        }
    }

    fun perpendicularLine(
        from: Coordinates,
        to: Coordinates,
        character: String? = null,
        fgColor: Color? = null,
        bgColor: Color? = null
    ) {
        layer.pixelsIn { coordinates ->
            coordinates.x in minOf(from.x, to.x)..maxOf(from.x, to.x) &&
                    coordinates.y in minOf(from.y, to.y)..maxOf(from.y, to.y)
        }.forEach { affectedPixel ->
            character?.let { affectedPixel.character = it }
            fgColor?.let { affectedPixel.fgColor = it }
            bgColor?.let { affectedPixel.bgColor = it }
        }
    }

    fun line(
        from: Coordinates,
        to: Coordinates,
        character: String? = null,
        fgColor: Color? = null,
        bgColor: Color? = null
    ) {
        val start = if (from.x <= to.x) from else to
        val end = if (start == from) to else from
        val slope = (end.y - start.y).toDouble() / (end.x - start.x).toDouble()

        val linePointCoordinates =
            if (slope.isInfinite()) {
                (start.y..end.y).map { y ->
                    Coordinates(start.x, y)
                }
            } else {
                (start.x..end.x).map { x ->
                    Coordinates(x, start.y + ((x - start.x).toFloat() * slope).roundToInt())
                }
            }

        linePointCoordinates
            .map { layer.pixelAt(it) }
            .forEach { affectedPixel ->
                character?.let { affectedPixel.character = it }
                fgColor?.let { affectedPixel.fgColor = it }
                bgColor?.let { affectedPixel.bgColor = it }
            }
    }
}
