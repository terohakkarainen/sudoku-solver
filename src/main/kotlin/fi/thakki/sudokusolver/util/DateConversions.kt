package fi.thakki.sudokusolver.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateConversions {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun toPrintable(dateTime: ZonedDateTime): String =
        dateTime.format(formatter)
}
