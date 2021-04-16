package fi.thakki.sudokusolver.application.canvas

const val ANSI_ESC = "\u001B"

@Suppress("unused")
enum class Color(val fgCode: String, val bgCode: String) {
    BLACK("30", "40"),
    RED("31", "41"),
    GREEN("32", "42"),
    YELLOW("33", "43"),
    BLUE("34", "44"),
    MAGENTA("35", "45"),
    CYAN("36", "46"),
    LIGHT_GRAY("37", "47"),
    DARK_GRAY("90", "100"),
    LIGHT_RED("91", "101"),
    LIGHT_GREEN("92", "102"),
    LIGHT_YELLOW("93", "103"),
    LIGHT_BLUE("94", "104"),
    LIGHT_MAGENTA("95", "105"),
    LIGHT_CYAN("96", "106"),
    WHITE("97", "107"),
    DEFAULT("39", "49");

    companion object {
        const val RESET = "$ANSI_ESC[0m"
    }
}
