package fi.thakki.sudokusolver.engine.model

import java.time.ZonedDateTime

data class RevisionInformation(
    val number: Int,
    val createdAt: ZonedDateTime,
    val description: String,
)
