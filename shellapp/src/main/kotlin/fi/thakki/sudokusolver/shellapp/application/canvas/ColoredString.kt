package fi.thakki.sudokusolver.shellapp.application.canvas

import fi.thakki.sudokusolvershellapp.application.canvas.ANSI_ESC
import fi.thakki.sudokusolvershellapp.application.canvas.Color

object ColoredString {

    fun of(s: String, colors: List<String>): String =
        "${preambleOf(colors)}$s${Color.RESET}"

    private fun preambleOf(colors: List<String>): String =
        colors
            .joinToString(";")
            .let { codes -> "$ANSI_ESC[${codes}m" }
}
