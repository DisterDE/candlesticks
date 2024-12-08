package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.InstantSerializer
import com.disterde.candlesticks.util.Price
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents a single candlestick in a candlestick chart.
 *
 * A candlestick encapsulates price movement data for a specific time interval
 * (e.g., 1 minute). It contains the opening and closing prices, as well as the
 * highest and lowest prices observed during that interval.
 *
 * ### Serialization:
 * - The `Instant` fields (`openTimestamp` and `closeTimestamp`) are serialized
 *   using a custom serializer (`InstantSerializer`) to ensure compatibility with
 *   JSON or other data formats.
 *
 * ### Mutable Fields:
 * - Some fields (`closeTimestamp`, `highPrice`, `lowPrice`, `closingPrice`) are mutable
 *   to allow updates during the candlestick's lifecycle.
 *
 * ### Fields:
 * - `openTimestamp`: The start time of the interval.
 * - `closeTimestamp`: The end time of the interval (mutable to support updates).
 * - `openPrice`: The first price observed in the interval.
 * - `highPrice`: The highest price observed in the interval.
 * - `lowPrice`: The lowest price observed in the interval.
 * - `closingPrice`: The last price observed in the interval.
 */
@Serializable
data class Candlestick(
    /**
     * The start time of the candlestick's interval.
     */
    @Serializable(with = InstantSerializer::class)
    val openTimestamp: Instant,

    /**
     * The end time of the candlestick's interval.
     * This field may be updated until the candlestick is finalized.
     */
    @Serializable(with = InstantSerializer::class)
    var closeTimestamp: Instant,

    /**
     * The first price observed in the interval.
     */
    val openPrice: Price,

    /**
     * The highest price observed in the interval.
     * This field is mutable to allow updates as new prices arrive.
     */
    var highPrice: Price,

    /**
     * The lowest price observed in the interval.
     * This field is mutable to allow updates as new prices arrive.
     */
    var lowPrice: Price,

    /**
     * The last price observed in the interval.
     * This field is mutable to allow updates as new prices arrive.
     */
    var closingPrice: Price
)