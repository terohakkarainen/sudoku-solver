package fi.thakki.sudokusolver.shellapp.application.canvas

import fi.thakki.sudokusolvershellapp.application.canvas.Color

class Pixel(
    initialValue: PixelValue? = null,
    var fgColor: Color? = null,
    var bgColor: Color? = null
) {
    var value: PixelValue? = initialValue
        set(newValue) {
            field = if (isNonNullBorder(value) && isNonNullBorder(newValue)) {
                PixelValue.Border.merge(
                    checkNotNull(value) as PixelValue.Border,
                    checkNotNull(newValue) as PixelValue.Border
                )
            } else newValue
        }

    private fun isNonNullBorder(value: PixelValue?) =
        value != null && value is PixelValue.Border
}
