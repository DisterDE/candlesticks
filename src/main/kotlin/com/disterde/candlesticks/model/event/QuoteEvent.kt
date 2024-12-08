package com.disterde.candlesticks.model.event

import com.disterde.candlesticks.model.Quote
import kotlinx.serialization.Serializable

/**
 * Represents a real-time quote update for a financial instrument.
 *
 * These events are typically received from the `/quotes` WebSocket stream
 * and contain the latest price data for a specific instrument.
 *
 * ### Fields:
 * - `data`: The `Quote` object containing the ISIN and the latest price of the instrument.
 *
 * ### Usage:
 * - This class is serialized/deserialized using `kotlinx.serialization`.
 * - It is used in systems that process real-time price updates to update candlestick data.
 */
@Serializable
data class QuoteEvent(
    /**
     * The latest quote data for the financial instrument.
     */
    val data: Quote
)