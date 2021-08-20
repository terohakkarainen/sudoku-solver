package fi.thakki.sudokusolver.webapp

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import fi.thakki.sudokusolver.engine.service.builder.StandardSudokuBuilder
import fi.thakki.sudokusolver.engine.service.message.DiscardingMessageBroker
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

internal class ApplicationTest {

    @Test
    fun `root page contents are ok`() {
        val sudoku = StandardSudokuBuilder(
            standardLayout = StandardSudokuBuilder.StandardLayout.STANDARD_9X9,
            messageBroker = DiscardingMessageBroker
        ).build()

        withTestApplication({ configureRouting(sudoku) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isNotNull().contains("SudokuSolver in state")
            }
        }
    }
}
