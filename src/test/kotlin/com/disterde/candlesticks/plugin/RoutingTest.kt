package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.service.CandlestickHandler
import com.disterde.candlesticks.service.HandlerManager
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

class RoutingTest {

    @Test
    fun `should process requests with and without ISIN correctly`() = testApplication {
        val handler = mockk<CandlestickHandler>(relaxed = true) {
            every { getCandlesticks() } returns CANDLESTICKS
        }
        val manager = mockk<HandlerManager>(relaxed = true) {
            every { getHandler(ISIN) } returns handler
        }

        application {
            install(Koin) {
                modules(module {
                    single { handler }
                    single { manager }
                })
            }
            configureRouting()
            configureSerialization()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        client.get("/candlesticks").apply {
            assertThat(status).isEqualTo(BadRequest)
            assertThat(Json.decodeFromString<Map<String, String>>(bodyAsText())).isEqualTo(MISSING_ISIN_MAP)
        }

        client.get("/candlesticks?isin=$ISIN").apply {
            assertThat(status).isEqualTo(HttpStatusCode.OK)
            assertThat(Json.decodeFromString<List<Candlestick>>(bodyAsText())).isEqualTo(CANDLESTICKS)
            verifyOrder {
                manager.getHandler(ISIN)
                handler.getCandlesticks()
            }
        }
    }

    companion object {
        private const val ISIN = "TEST_ISIN"
        private val MISSING_ISIN_MAP = mapOf("reason" to "missing_isin")
        private val TIMESTAMP = Instant.now().truncatedTo(MINUTES)
        private val CANDLESTICKS = listOf(
            Candlestick(
                openTimestamp = TIMESTAMP,
                closeTimestamp = TIMESTAMP.plus(1, MINUTES),
                openPrice = 1689.5,
                highPrice = 1689.5,
                lowPrice = 1492.0,
                closingPrice = 1492.0
            )
        )
    }
}