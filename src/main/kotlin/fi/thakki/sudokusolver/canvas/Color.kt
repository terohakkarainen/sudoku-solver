package fi.thakki.sudokusolver.canvas

const val ANSI_ESC = "\u001B"

enum class Color(val fgCode: String, val bgCode: String) {
    BLACK("30", "40"),
    RED("31", "41"),
    GREEN("32", "42"),
    YELLOW("33", "43"),
    BLUE("34", "44"),
    MAGENTA("35", "45"),
    CYAN("36", "46"),
    LIGHT_GRAY("37", "47"),
    WHITE("97", "107"),
    DEFAULT("39", "49");

    companion object {
        const val RESET = "$ANSI_ESC[0m"
    }
}
