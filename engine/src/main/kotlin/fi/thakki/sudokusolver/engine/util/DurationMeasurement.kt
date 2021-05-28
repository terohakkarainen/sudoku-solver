package fi.thakki.sudokusolver.engine.util

import java.time.Duration
import java.time.Instant

class DurationMeasurement<T : Any> {

    data class Result<T : Any>(
        val duration: Duration,
        val result: T?
    )

    fun durationOf(operation: () -> T?): Result<T> =
        Instant.now().let { startingTime ->
            val result = operation()
            Result(durationSince(startingTime), result)
        }

    private fun durationSince(instant: Instant): Duration =
        Duration.between(instant, Instant.now())
}
