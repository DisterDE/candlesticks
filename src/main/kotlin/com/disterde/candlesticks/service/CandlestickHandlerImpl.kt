package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.util.Price
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class CandlestickHandlerImpl : CandlestickHandler {

    private val incomingPrices = ConcurrentLinkedQueue<Price>()

    private val candlestickBuffer = arrayOfNulls<Candlestick>(29)
    private var candleCount = 0
    private var currentIndex = 0

    private val candlesList = AtomicReference<List<Candlestick>>(emptyList())

    private var openPrice: Price? = null
    private var closePrice: Price? = null
    private var minPrice: Price? = null
    private var maxPrice: Price? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            var currentMinute = LocalDateTime.now().withSecond(0).withNano(0)

            while (isActive) {
                val now = LocalDateTime.now()
                val nextMinute = now.withSecond(0).withNano(0).plusMinutes(1)
                val delayMs = Duration.between(now, nextMinute).toMillis()
                delay(delayMs.coerceAtLeast(0))

                fillPrices()

                val openT = currentMinute.toInstant(ZoneOffset.UTC)
                val closeT = currentMinute.plusMinutes(1).toInstant(ZoneOffset.UTC)
                val lastCandle = candlesList.get().lastOrNull()
                val newCandle = createCandle(openT, closeT, lastCandle)

                newCandle?.let { candle ->
                    candlestickBuffer[currentIndex] = candle
                    if (candleCount < BUFFER_SIZE) {
                        candleCount++
                    }
                    currentIndex = (currentIndex + 1) % BUFFER_SIZE

                    val recentCandles = buildList {
                        val start = if (candleCount == BUFFER_SIZE) currentIndex else 0
                        for (i in 0..<candleCount) {
                            val idx = (start + i) % BUFFER_SIZE
                            val c = candlestickBuffer[idx]!!
                            add(c)
                        }
                    }
                    candlesList.set(recentCandles)
                }

                openPrice = null
                closePrice = null
                minPrice = null
                maxPrice = null

                currentMinute = nextMinute
            }
        }
    }

    private fun createCandle(openT: Instant, closeT: Instant, prevCandle: Candlestick?): Candlestick? {
        return if (openPrice == null) {
            prevCandle?.copy(
                openTimestamp = openT,
                closeTimestamp = closeT
            )
        } else {
            Candlestick(
                openTimestamp = openT,
                closeTimestamp = closeT,
                openPrice = openPrice!!,
                highPrice = maxPrice!!,
                lowPrice = minPrice!!,
                closingPrice = closePrice!!
            )
        }
    }

    private fun fillPrices() {
        var p = incomingPrices.poll()
        while (p != null) {
            if (openPrice == null) {
                openPrice = p
                closePrice = p
                minPrice = p
                maxPrice = p
            } else {
                closePrice = p
                if (p < minPrice!!) minPrice = p
                if (p > maxPrice!!) maxPrice = p
            }
            p = incomingPrices.poll()
        }
    }

    override fun addPrice(price: Price) {
        incomingPrices.offer(price)
    }

    override fun getCandlesticks(): List<Candlestick> {
        val closed = candlesList.get()

        val now = LocalDateTime.now().withSecond(0).withNano(0)
        val openT = now.toInstant(ZoneOffset.UTC)
        val closeT = now.plusMinutes(1).toInstant(ZoneOffset.UTC)
        val lastCandle = closed.lastOrNull()

        val current = createCandle(openT, closeT, lastCandle)

        return if (current != null) {
            ArrayList<Candlestick>(closed.size + 1).apply {
                addAll(closed)
                add(current)
            }
        } else {
            closed
        }
    }

    override fun stop() {
        scope.cancel()
    }

    companion object {
        private const val BUFFER_SIZE = 29
    }
}