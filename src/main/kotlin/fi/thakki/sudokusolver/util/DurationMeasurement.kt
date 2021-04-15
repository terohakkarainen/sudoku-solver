package fi.thakki.sudokusolver.util

import java.time.Duration
import java.time.Instant

object DurationMeasurement {

    fun <T : Any> durationOf(operation: () -> T?): Pair<Duration, T?> =
        Instant.now().let { startingTime ->
            val result = operation()
            Pair(durationSince(startingTime), result)
        }

    private fun durationSince(instant: Instant): Duration =
        Duration.between(instant, Instant.now())
}
