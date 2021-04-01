package fi.thakki.sudokusolver.canvas

import fi.thakki.sudokusolver.model.Coordinates
import kotlin.reflect.KProperty1

class Canvas(private val size: Size, numberOfLayers: Int) {

    val layers: List<Layer> = List(numberOfLayers) { Layer(size) }

    fun painterForLayer(layer: Layer): Painter =
        Painter(layer)

    fun copyToScreen() =
        (size.height - 1 downTo 0).forEach { y ->
            println(
                (0 until size.width).toList()
                    .map { x -> mergedFromLayers(Coordinates(x, y)) }
                    .joinToString(separator = "") { pixel -> toScreen(pixel) }
            )
        }

    private fun mergedFromLayers(coordinates: Coordinates): Pixel =
        layers
            .map { layer -> layer.pixelAt(coordinates) }
            .let { pixelsFromTopToBottom ->
                Pixel(
                    characterString = topmost(pixelsFromTopToBottom.iterator(), Pixel::character) ?: Pixel.NO_VALUE,
                    fgColor = topmost(pixelsFromTopToBottom.iterator(), Pixel::fgColor),
                    bgColor = topmost(pixelsFromTopToBottom.iterator(), Pixel::bgColor)
                )
            }

    private fun <T : Any> topmost(iterator: Iterator<Pixel>, property: KProperty1<Pixel, T?>): T? {
        while (iterator.hasNext()) {
            property.get(iterator.next())?.let { return it }
        }
        return null
    }

    private fun toScreen(pixel: Pixel): String =
        "${colorsOf(pixel)}${pixel.character}${Color.RESET}"

    private fun colorsOf(pixel: Pixel): String =
        listOfNotNull(pixel.bgColor?.bgCode, pixel.fgColor?.fgCode)
            .joinToString(";")
            .let { codes ->
                "$ANSI_ESC[${codes}m"
            }
}
