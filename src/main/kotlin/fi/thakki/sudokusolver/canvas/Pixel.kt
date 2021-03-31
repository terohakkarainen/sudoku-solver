package fi.thakki.sudokusolver.canvas

class Pixel(
    characterString: String? = null,
    var fgColor: Color? = null,
    var bgColor: Color? = null
) {
    var character: String? = characterString
        set(newValue) {
            field = mergeValues(field, newValue)
        }

    companion object {
        const val VERT_LIGHT_LINE = "\u250a"
        const val HORIZ_LIGHT_LINE = "\u2508"
        const val VERT_HEAVY_LINE = "\u2551"
        const val HORIZ_HEAVY_LINE = "\u2550"
        private const val LIGHT_CROSS_SECTION = "\u253c"
        private const val HEAVY_CROSS_SECTION = "\u254B"

        private fun mergeValues(oldValue: String?, newValue: String?): String? =
            if (oldValue != null && newValue != null) {
                when {
                    oldValue == VERT_LIGHT_LINE && newValue == HORIZ_LIGHT_LINE -> LIGHT_CROSS_SECTION
                    oldValue == HORIZ_LIGHT_LINE && newValue == VERT_LIGHT_LINE -> LIGHT_CROSS_SECTION
                    oldValue == VERT_HEAVY_LINE && newValue == HORIZ_HEAVY_LINE -> HEAVY_CROSS_SECTION
                    oldValue == HORIZ_HEAVY_LINE && newValue == VERT_HEAVY_LINE -> HEAVY_CROSS_SECTION
                    else -> newValue
                }
            } else newValue
    }
}
