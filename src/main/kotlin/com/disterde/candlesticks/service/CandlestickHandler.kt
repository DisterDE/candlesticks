package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.model.TimedPrice

/**
 * Interface for handling candlestick data for financial instruments.
 *
 * This handler is responsible for:
 * - Receiving price updates in real-time.
 * - Aggregating price updates into 1-minute candlesticks.
 * - Storing and managing the most recent candlesticks for efficient retrieval.
 * - Safely handling concurrent access to data in a multithreaded environment.
 */
interface CandlestickHandler {

    /**
     * Processes a new price update.
     *
     * This method accepts a timestamped price and updates the internal state to reflect
     * the changes.
     * Prices are aggregated into candlesticks based on 1-minute intervals.
     * The method is asynchronous to support high-throughput price streams.
     *
     * @param price The new price update containing the price and its associated timestamp.
     */
    suspend fun addPrice(price: TimedPrice)

    /**
     * Retrieves the list of aggregated candlesticks.
     *
     * This method returns the most recent candlesticks, including the current (in-progress)
     * candlestick if it exists.
     * The list contains a maximum of `maxCandles` items.
     * The returned list is a copy of the internal state to ensure thread safety and immutability
     * for external consumers.
     *
     * @return A list of the most recent candlesticks, sorted by time in ascending order.
     */
    fun getCandlesticks(): List<Candlestick>

    /**
     * Stops the handler and releases resources.
     *
     * This method stops all active coroutines and closes any open channels used by the handler.
     * It ensures that the handler is safely terminated without leaving resources hanging.
     * Should be called when the handler is no longer needed.
     */
    fun stop()
}