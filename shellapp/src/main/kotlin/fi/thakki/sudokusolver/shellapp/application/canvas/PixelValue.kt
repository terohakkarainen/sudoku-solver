package fi.thakki.sudokusolver.shellapp.application.canvas

interface Printable {
    fun printableValue(): String
}

sealed class PixelValue : Printable {

    data class Border(val code: String) : PixelValue() {
        override fun printableValue(): String = code

        companion object {
            const val VERT_LIGHT_LINE = "\u250a"
            const val HORIZ_LIGHT_LINE = "\u2508"
            const val VERT_HEAVY_LINE = "\u2551"
            const val HORIZ_HEAVY_LINE = "\u2550"
            private const val LIGHT_CROSS_SECTION = "\u253c"
            private const val HEAVY_CROSS_SECTION = "\u254B"

            fun merge(old: Border, new: Border): Border =
                when {
                    old.code == VERT_LIGHT_LINE && new.code == HORIZ_LIGHT_LINE ->
                        Border(LIGHT_CROSS_SECTION)
                    old.code == HORIZ_LIGHT_LINE && new.code == VERT_LIGHT_LINE ->
                        Border(LIGHT_CROSS_SECTION)
                    old.code == VERT_HEAVY_LINE && new.code == HORIZ_HEAVY_LINE ->
                        Border(HEAVY_CROSS_SECTION)
                    old.code == HORIZ_HEAVY_LINE && new.code == VERT_HEAVY_LINE ->
                        Border(HEAVY_CROSS_SECTION)
                    else -> new
                }
        }
    }

    data class Character(private val char: Char) : PixelValue() {
        override fun printableValue(): String = char.toString()
    }

    companion object {
        val NO_VALUE = Character(' ')
    }
}
