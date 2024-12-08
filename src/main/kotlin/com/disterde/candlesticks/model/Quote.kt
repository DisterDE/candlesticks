package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.ISIN
import com.disterde.candlesticks.util.Price
import kotlinx.serialization.Serializable

/**
 * Represents a real-time price update (quote) for a financial instrument.
 *
 * This class contains the essential details of a quote:
 * - The unique identifier of the instrument (`ISIN`).
 * - The latest price of the instrument.
 *
 * ### Fields:
 * - `isin`: The International Securities Identification Number (ISIN) uniquely identifying the instrument.
 * - `price`: The current price of the instrument with arbitrary precision.
 *
 * ### Usage:
 * - This class is serialized/deserialized using `kotlinx.serialization`.
 * - It is typically used in systems processing real-time price updates, such as updating candlestick data.
 *
 * ### Examples:
 * ```kotlin
 * val quote = Quote(
 *     isin = "US0378331005",
 *     price = 150.75
 * )
 * ```
 */
@Serializable
data class Quote(
    /**
     * The unique International Securities Identification Number (ISIN) for the instrument.
     */
    val isin: ISIN,

    /**
     * The latest price of the instrument.
     */
    val price: Price
)