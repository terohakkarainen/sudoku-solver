package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates
import kotlin.math.roundToInt

class Painter(val layer: Layer) {

    fun pixel(
        value: PixelValue?,
        coordinates: Coordinates,
        fgColor: Color? = null,
        bgColor: Color? = null
    ) {
        with(layer.pixelAt(coordinates)) {
            value?.let { this.value = it }
            fgColor?.let { this.fgColor = it }
            bgColor?.let { this.bgColor = it }
        }
    }

    fun filledRectangle(rectangle: Rectangle, bgColor: Color) {
        layer.pixelsIn(rectangle)
            .forEach { affectedPixel ->
                affectedPixel.bgColor = bgColor
            }
    }

    fun characterRectangle(rectangle: Rectangle, character: PixelValue.Character) {
        layer.pixelsIn(rectangle)
            .forEach { affectedPixel ->
                affectedPixel.value = character
            }
    }

    fun perpendicularLine(
        from: Coordinates,
        to: Coordinates,
        value: PixelValue? = null,
        fgColor: Color? = null,
        bgColor: Color? = null
    ) {
        layer.pixelsIn { coordinates ->
            coordinates.x in minOf(from.x, to.x)..maxOf(from.x, to.x) &&
                    coordinates.y in minOf(from.y, to.y)..maxOf(from.y, to.y)
        }.forEach { affectedPixel ->
            value?.let { affectedPixel.value = it }
            fgColor?.let { affectedPixel.fgColor = it }
            bgColor?.let { affectedPixel.bgColor = it }
        }
    }

    fun freeFormLine(
        from: Coordinates,
        to: Coordinates,
        value: PixelValue? = null,
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

        linePointCoordinates.forEach { coordinates ->
            pixel(value, coordinates, fgColor, bgColor)
        }
    }
}
