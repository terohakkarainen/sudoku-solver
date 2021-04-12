package fi.thakki.sudokusolver.canvas

object ColoredString {

    fun of(s: String, colors: List<String>): String =
        "${preambleOf(colors)}$s${Color.RESET}"

    private fun preambleOf(colors: List<String>): String =
        colors
            .joinToString(";")
            .let { codes -> "$ANSI_ESC[${codes}m" }
}
