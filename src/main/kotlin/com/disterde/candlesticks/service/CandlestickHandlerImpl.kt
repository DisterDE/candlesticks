package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.CandleState
import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.model.TimedPrice
import com.disterde.candlesticks.util.ISIN
import com.disterde.candlesticks.util.Price
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Implementation of a candlestick handler for a specific ISIN.
 *
 * This class processes incoming price updates and generates candlesticks in 1-minute intervals.
 * The most recent `maxCandles` candlesticks are retained, and excess data is discarded.
 *
 * ### Key Features:
 * - Efficient handling of price updates using a `Channel` to serialize updates.
 * - Internal state management using mutable collections for simplicity and performance.
 * - Copying of data during retrieval to ensure external immutability.
 *
 * ### Scalability Considerations:
 * While the current implementation is optimized for a small number of candlesticks (`maxCandles`),
 * the following adjustments could be made to handle higher loads or larger data requirements:
 *
 * 1. **Increased `maxCandles`:**
 *    - Replace the `MutableList` with a more scalable data structure, such as a ring buffer or `Deque`.
 *    - Use an indexed structure (e.g., `LinkedHashMap` with `openTimestamp` as key) to optimize lookups.
 *
 * 2. **Handling High-Volume Updates:**
 *    - Increase `Channel` capacity to handle bursts of incoming data.
 *    - Use a batching mechanism to group updates before processing.
 *
 * 3. **Distributed State Management:**
 *    - For horizontal scaling, shard state by ISIN and distribute processing across multiple nodes.
 *    - Use a distributed cache (e.g., Redis) to store candlesticks for global access.
 *
 * 4. **Asynchronous Data Storage:**
 *    - Persist closed candlesticks to a database or file system for long-term storage.
 *    - Keep only the most recent `maxCandles` in memory for fast access.
 *
 * 5. **High-Frequency Reading Optimization:**
 *    - If reads vastly outnumber writings,
 *    cache the result of `getCandlesticks()` and update it only on state changes.
 */
class CandlestickHandlerImpl(
    private val isin: ISIN,
    private val maxCandles: Int,
) : CandlestickHandler {

    private val log = KotlinLogging.logger {}
    private val scope = CoroutineScope(Dispatchers.Default)

    // Mutable state for storing candlesticks
    private val state = CandleState()

    // Channel for sequentially processing price updates and commands
    private val eventChannel = Channel<PriceMessage>(
        capacity = BUFFERED,
        onUndeliveredElement = {
            log.warn { "[$isin] Unprocessed element dropped: $it" }
        }
    )

    // Messages for the channel
    private sealed class PriceMessage
    private data class NewPrice(val price: TimedPrice) : PriceMessage()
    private data class CloseCandle(val closeTimestamp: Instant) : PriceMessage()

    init {
        // Coroutine to close the current candle at the end of each minute
        scope.launch {
            var currentMinuteStart = LocalDateTime.now().withSecond(0).withNano(0)
            while (isActive) {
                val nextMinuteStart = currentMinuteStart.plusMinutes(1)
                val delayMs = Duration.between(LocalDateTime.now(), nextMinuteStart).toMillis()
                delay(delayMs.coerceAtLeast(0))
                closeCurrentCandle(nextMinuteStart.toInstant(ZoneOffset.UTC))
                currentMinuteStart = nextMinuteStart
            }
        }

        // Coroutine to process events from the channel
        scope.launch {
            for (event in eventChannel) {
                handleEvent(event)
            }
        }
    }

    /**
     * Processes incoming events.
     */
    private fun handleEvent(event: PriceMessage) {
        when (event) {
            is NewPrice -> {
                log.trace { "[$isin] Processing new price event: $event" }
                handleNewPrice(event.price)
            }

            is CloseCandle -> {
                log.trace { "[$isin] Processing close candle event: $event" }
                handleCloseCandle(event.closeTimestamp)
            }
        }
    }

    /**
     * Closes the current candle or creates a new one if no data was received.
     *
     * ### Behavior:
     * - **No current candle:** Creates a new candle using the last closed candle:
     *   - `openPrice` and `closingPrice` are set to the last `closingPrice`.
     *   - `highPrice` and `lowPrice` preserve the last candle's range.
     * - **Current candle exists:** Finalizes and moves it to the closed list.
     *
     * ### Example:
     * - **Last closed candle:**
     *   ```
     *   { open: 100.0, high: 105.0, low: 98.0, close: 102.0 }
     *   ```
     * - **New candle (no data):**
     *   ```
     *   { open: 102.0, high: 105.0, low: 98.0, close: 102.0 }
     *   ```
     *
     * @param closeTimestamp The timestamp for the candle's closure.
     */
    private fun handleCloseCandle(closeTimestamp: Instant) {
        val (closedCandles, currentCandle) = state
        if (currentCandle == null) {
            closedCandles.lastOrNull()?.let { lastCandle ->
                val newCandle = Candlestick(
                    openTimestamp = lastCandle.closeTimestamp,
                    closeTimestamp = closeTimestamp,
                    openPrice = lastCandle.closingPrice,
                    highPrice = lastCandle.highPrice,
                    lowPrice = lastCandle.lowPrice,
                    closingPrice = lastCandle.closingPrice
                )
                closedCandles += newCandle
                while (closedCandles.size > maxCandles) closedCandles.removeFirst()
            }
        } else if (currentCandle.closeTimestamp == closeTimestamp) {
            closedCandles += currentCandle
            while (closedCandles.size > maxCandles) closedCandles.removeFirst()
            state.currentCandle = null
        }
        log.debug { "[$isin] Closed candle at $closeTimestamp" }
    }

    /**
     * Handles a new incoming price by updating the current or closed candles.
     */
    private fun handleNewPrice(timedPrice: TimedPrice) {
        val (price, timestamp) = timedPrice
        val priceMinuteStart = timestamp.truncatedTo(ChronoUnit.MINUTES)
        val nextMinuteStart = priceMinuteStart.plus(1, ChronoUnit.MINUTES)

        state.currentCandle?.let { current ->
            when (priceMinuteStart) {
                current.openTimestamp -> {
                    current.apply {
                        closingPrice = price
                        highPrice = maxOf(highPrice, price)
                        lowPrice = minOf(lowPrice, price)
                    }
                    log.trace { "[$isin] Updated current candle with price: $price at $timestamp" }
                }

                current.closeTimestamp -> {
                    val newCandle = createCandle(priceMinuteStart, nextMinuteStart, price)
                    state.closedCandles += current
                    while (state.closedCandles.size > maxCandles) state.closedCandles.removeFirst()
                    state.currentCandle = newCandle
                    log.debug { "[$isin] Closed current candle and created new one for $priceMinuteStart" }
                }

                else -> updateClosedCandles(price, priceMinuteStart)
            }
        } ?: run {
            state.currentCandle = createCandle(priceMinuteStart, nextMinuteStart, price)
            log.debug { "[$isin] Created new candle for $priceMinuteStart with price: $price" }
        }
    }

    /**
     * Updates a closed candle if the price corresponds to an already closed minute.
     */
    private fun updateClosedCandles(price: Price, priceMinuteStart: Instant) {
        val closedCandles = state.closedCandles
        val index = closedCandles.indexOfLast { it.openTimestamp == priceMinuteStart }
        if (index != -1) {
            closedCandles[index].apply {
                closingPrice = price
                highPrice = maxOf(highPrice, price)
                lowPrice = minOf(lowPrice, price)
            }
            log.debug { "[$isin] Updated closed candle for $priceMinuteStart with price: $price" }
        } else {
            log.warn { "[$isin] Price received for unknown candle: $priceMinuteStart" }
        }
    }

    /**
     * Adds a new price to the handler.
     */
    override suspend fun addPrice(price: TimedPrice) {
        eventChannel.send(NewPrice(price))
        log.trace { "[$isin] Added price to event channel: $price" }
    }

    /**
     * Returns the list of candlesticks, including the current one if it exists.
     *
     * Copies are returned to prevent external consumers from mutating internal state.
     */
    override fun getCandlesticks(): List<Candlestick> {
        val (closedCandles, currentCandle) = state
        return (currentCandle?.let { closedCandles + it } ?: closedCandles)
            .takeLast(maxCandles)
            .map { it.copy() }
            .also { log.trace { "[$isin] Returning candlesticks: $it" } }
    }

    /**
     * Stops the handler and cleans up resources.
     */
    override fun stop() {
        scope.cancel()
        eventChannel.close()
        log.info { "[$isin] Stopped handler" }
    }

    /**
     * Sends a command to close the current candle.
     */
    private suspend fun closeCurrentCandle(closeTimestamp: Instant) {
        eventChannel.send(CloseCandle(closeTimestamp))
        log.trace { "[$isin] Sent close candle event for timestamp: $closeTimestamp" }
    }

    /**
     * Creates a new candlestick.
     */
    private fun createCandle(
        openT: Instant,
        closeT: Instant,
        price: Price
    ) = Candlestick(
        openTimestamp = openT,
        closeTimestamp = closeT,
        openPrice = price,
        closingPrice = price,
        highPrice = price,
        lowPrice = price
    ).also {
        log.trace { "[$isin] New candle created: $it" }
    }
}