package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.TimedPrice
import com.disterde.candlesticks.model.event.InstrumentEvent
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.ADD
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.DELETE
import com.disterde.candlesticks.model.event.QuoteEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.time.Instant

/**
 * WebSocket listener for real-time instrument and quote updates.
 *
 * ### Overview:
 * This class listens to WebSocket streams for:
 * - Quote updates (`/quotes` endpoint).
 * - Instrument add/delete events (`/instruments` endpoint).
 *
 * The received events are processed asynchronously via dedicated `Channel`s, ensuring efficient
 * separation of WebSocket reading and event processing.
 *
 * ### Design Decisions:
 * - **Channels:**
 *   Used to decouple WebSocket event reading and processing, allowing WebSocket threads to remain unblocked.
 * - **Coroutines for Processing:**
 *   Dedicated coroutines process events from the channels, providing scalability for event handling.
 * - **Automatic Retry:**
 *   In case of WebSocket disconnection or errors, reconnection is attempted after a configurable delay.
 *
 * ### Responsibilities:
 * - Connect to WebSocket streams and receive real-time updates.
 * - Route updates to the appropriate `HandlerManager`.
 * - Maintain resiliency through retry logic and structured error handling.
 */
class MessageListenerImpl(
    private val client: HttpClient,
    private val manager: HandlerManager
) : MessageListener {

    private val log = KotlinLogging.logger {}
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var active = true

    // Channels for quote and instrument processing
    private val quoteChannel = Channel<QuoteEvent>(Channel.UNLIMITED)
    private val instrumentChannel = Channel<InstrumentEvent>(Channel.UNLIMITED)

    /**
     * Starts the WebSocket listener and initializes processing channels.
     *
     * This method:
     * - Establishes connections to the WebSocket endpoints.
     * - Launches dedicated coroutines for event processing.
     */
    override fun start() {
        log.info { "Starting WebSocket message listener..." }
        launchWebSocketProcessor(QUOTES, ::handleQuoteEvent, "quotes")
        launchWebSocketProcessor(INSTRUMENTS, ::handleInstrumentEvent, "instruments")

        // Launch a coroutine for processing quotes
        scope.launch {
            for (event in quoteChannel) {
                try {
                    val quote = event.data
                    val timestamp = Instant.now()
                    manager.getHandler(quote.isin).addPrice(TimedPrice(quote.price, timestamp))
                } catch (e: Exception) {
                    log.error(e) { "Failed to process quote event: ${e.message}" }
                }
            }
        }

        // Launch a coroutine for processing instruments
        scope.launch {
            for (event in instrumentChannel) {
                try {
                    val (type, data) = event
                    when (type) {
                        ADD -> {
                            log.info { "Adding handler for ISIN: ${data.isin}" }
                            runCatching { manager.createHandler(data.isin) }
                                .onFailure { log.error(it) { "Failed to add handler for ISIN: ${data.isin}" } }
                        }

                        DELETE -> {
                            log.info { "Removing handler for ISIN: ${data.isin}" }
                            runCatching { manager.deleteHandler(data.isin) }
                                .onFailure { log.error(it) { "Failed to remove handler for ISIN: ${data.isin}" } }
                        }
                    }
                } catch (e: Exception) {
                    log.error(e) { "Failed to process instrument event: ${e.message}" }
                }
            }
        }
    }

    /**
     * Stops the WebSocket listener and closes processing channels.
     */
    override fun stop() {
        active = false
        scope.cancel()
        quoteChannel.close()
        instrumentChannel.close()
        log.info { "Message listener stopped." }
    }

    /**
     * Launches a WebSocket processor for a specific endpoint.
     *
     * @param path The WebSocket path (e.g., `/quotes` or `/instruments`).
     * @param processor The function to handle incoming events.
     * @param name A name for logging purposes.
     */
    private inline fun <reified T : Any> launchWebSocketProcessor(
        path: String,
        crossinline processor: suspend (T) -> Unit,
        name: String
    ) {
        scope.launch {
            while (active) {
                try {
                    client.webSocket(host = HOST, port = PORT, path = path) {
                        log.info { "Connected to $name WebSocket at $HOST:$PORT$path" }
                        while (active) {
                            val event = receiveDeserialized<T>()
                            processor(event)
                        }
                    }
                } catch (e: Exception) {
                    log.error(e) { "Error in $name WebSocket connection: ${e.message}" }
                    delay(RETRY_DELAY)
                }
            }
        }
    }

    /**
     * Handles a quote event and sends it to the quote channel.
     *
     * @param event The deserialized `QuoteEvent` containing quote data.
     */
    private suspend fun handleQuoteEvent(event: QuoteEvent) {
        try {
            quoteChannel.send(event)
        } catch (e: Exception) {
            log.error(e) { "Failed to enqueue quote event: ${e.message}" }
        }
    }

    /**
     * Handles an instrument event and sends it to the instrument channel.
     *
     * @param event The deserialized `InstrumentEvent` containing instrument data.
     */
    private suspend fun handleInstrumentEvent(event: InstrumentEvent) {
        try {
            instrumentChannel.send(event)
        } catch (e: Exception) {
            log.error(e) { "Failed to enqueue instrument event: ${e.message}" }
        }
    }

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 8032
        private const val QUOTES = "/quotes"
        private const val INSTRUMENTS = "/instruments"
        private const val RETRY_DELAY = 1000L // Retry delay in milliseconds
    }
}