package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates

class Layer(val size: Size, val zIndex: Int) {

    private val pixels: Map<Coordinates, Pixel> =
        (0 until size.width).flatMap { x ->
            (0 until size.height).map { y ->
                Pair(x, y)
            }
        }.map { xyPair ->
            Coordinates(xyPair.first, xyPair.second) to Pixel()
        }.toMap()

    fun pixelAt(coordinates: Coordinates): Pixel =
        checkNotNull(pixels[coordinates]) {
            "No pixel exists in $coordinates"
        }

    fun pixels(): Collection<Pixel> =
        pixels.values

    fun pixelsIn(predicate: (Coordinates) -> Boolean): Collection<Pixel> =
        pixels.keys
            .filter(predicate)
            .map { key ->
                checkNotNull(pixels[key])
            }
}
