package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates
import fi.thakki.sudokusolver.model.Size

class Layer(val size: Size) {

    private val pixels: Map<Coordinates, Pixel> =
        (0 until size.width).flatMap { x ->
            (0 until size.height).map { y ->
                Pair(x, y)
            }
        }.map { xyPair ->
            Coordinates(xyPair.first, xyPair.second) to Pixel()
        }.toMap()

    fun pixelAt(coordinates: Coordinates): Pixel =
        checkNotNull(pixels[coordinates]) { "No pixel exists in $coordinates" }

    fun pixelsIn(rectangle: Rectangle): Collection<Pixel> =
        pixelsIn { coordinates ->
            coordinates.x >= rectangle.bottomLeft.x && coordinates.x <= rectangle.topRight.x &&
                    coordinates.y >= rectangle.bottomLeft.y && coordinates.y <= rectangle.topRight.y
        }

    fun pixelsIn(predicate: (Coordinates) -> Boolean): Collection<Pixel> =
        pixels.keys
            .filter(predicate)
            .map { key ->
                checkNotNull(pixels[key])
            }
}
