package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.Instrument
import com.disterde.candlesticks.model.Quote
import com.disterde.candlesticks.model.event.InstrumentEvent
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.ADD
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.DELETE
import com.disterde.candlesticks.model.event.QuoteEvent
import io.ktor.client.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessageListenerImplTest {
    private val client = HttpClient {
        install(io.ktor.client.plugins.websocket.WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(
                Json { ignoreUnknownKeys = true }
            )
        }
    }

    private val manager = mockk<HandlerManager>(relaxed = true)
    private lateinit var listener: MessageListenerImpl

    @BeforeTest
    fun setUp() {
        listener = MessageListenerImpl(client, manager, HOST, PORT)
    }

    @Test
    fun `should create handler when receiving ADD instrument event`() = runBlocking {
        val server = createServer(
            "/instruments",
            listOf(ADD_EVENT.toJson())
        )
        server.start()
        listener.start()
        delay(300)
        listener.stop()
        server.stop()

        verify(exactly = 1) { manager.createHandler(ISIN) }
    }

    @Test
    fun `should delete handler when receiving DELETE instrument event`() = runBlocking {
        val server = createServer(
            "/instruments",
            listOf(DELETE_EVENT.toJson())
        )
        server.start()
        listener.start()
        delay(300)
        listener.stop()
        server.stop()

        verify(exactly = 1) { manager.deleteHandler(ISIN) }
    }

    @Test
    fun `should process multiple instrument events in order`() = runBlocking {
        val server = createServer(
            "/instruments",
            listOf(ADD_EVENT.toJson(), DELETE_EVENT.toJson())
        )
        server.start()
        listener.start()
        delay(300)
        listener.stop()
        server.stop()

        verifyOrder {
            manager.createHandler(ISIN)
            manager.deleteHandler(ISIN)
        }
    }

    @Test
    fun `should get handler by isin when receiving quote event`() = runBlocking {
        val server = createServer(
            "/quotes",
            listOf(QUOTE_EVENT.toJson())
        )
        server.start()
        listener.start()
        delay(300)
        listener.stop()
        server.stop()

        verify(exactly = 1) { manager.getHandler(ISIN) }
    }

    @Test
    fun `should process multiple QuoteEvents correctly`() = runBlocking {
        val server = createServer(
            "/quotes",
            listOf(QUOTE_EVENT.toJson(), QUOTE_EVENT.toJson())
        )
        server.start()
        listener.start()
        delay(300)
        listener.stop()
        server.stop()

        verify(exactly = 2) { manager.getHandler(ISIN) }
    }

    @Test
    fun `should not process events after stop`() = runBlocking {
        val server = createServer(
            "/quotes",
            listOf(QUOTE_EVENT.toJson())
        )
        server.start()
        listener.start()
        listener.stop()
        delay(300)
        server.stop()

        verify(exactly = 0) { manager.getHandler(ISIN) }
        verify(exactly = 0) { manager.createHandler(any()) }
        verify(exactly = 0) { manager.deleteHandler(any()) }
    }

    private fun createServer(
        path: String,
        messages: List<String>
    ): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
        return embeddedServer(
            CIO,
            port = PORT,
            module = {
                install(WebSockets)

                routing {
                    webSocket(path) {
                        messages.forEach { send(it) }
                        close()
                    }
                }
            }
        )
    }

    private inline fun <reified T : Any> T.toJson(): String = Json.encodeToString(this)

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 8080
        private const val ISIN = "TEST_ISIN"
        private const val PRICE = 1.0
        private val INSTRUMENT = Instrument(ISIN, "")
        private val ADD_EVENT = InstrumentEvent(ADD, INSTRUMENT)
        private val DELETE_EVENT = InstrumentEvent(DELETE, INSTRUMENT)
        private val QUOTE = Quote(ISIN, PRICE)
        private val QUOTE_EVENT = QuoteEvent(QUOTE)
    }
}