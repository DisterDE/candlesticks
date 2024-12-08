package com.disterde.candlesticks.model

/**
 * Represents the state of candlesticks for a specific instrument.
 *
 * This class holds:
 * - A list of closed candlesticks (`closedCandles`) that have been finalized.
 * - A reference to the current candlestick (`currentCandle`), which is being updated
 *   based on incoming price updates.
 *
 * The `closedCandles` list is mutable to allow efficient addition and removal of items
 * as new candlesticks are closed and the oldest ones are discarded.
 *
 * ### Notes:
 * - `currentCandle` can be `null` if no prices have been received yet or the previous candle
 *   has just been closed and a new one is not yet initialized.
 * - This class is not thread-safe and is intended to be used within a single-threaded context
 *   (e.g., inside a coroutine processing loop).
 */
data class CandleState(
    /**
     * A list of finalized candlesticks.
     * This list holds the most recent closed candlesticks up to a predefined maximum size.
     */
    val closedCandles: MutableList<Candlestick> = mutableListOf(),

    /**
     * The current candlestick being updated.
     * This represents the candlestick for the ongoing 1-minute interval.
     */
    var currentCandle: Candlestick? = null
)