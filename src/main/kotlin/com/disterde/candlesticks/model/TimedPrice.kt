package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.Price
import java.time.Instant

/**
 * Represents a price update with an associated timestamp.
 *
 * This class encapsulates a price (`Price`) and the time (`Instant`) at which
 * the price was reported.
 * It is used to track price changes over time
 * and serves as the primary input for candlestick aggregation.
 *
 * ### Fields:
 * - `price`: The reported price value.
 * - `timestamp`: The time at which the price was reported.
 *
 * ### Notes:
 * - Instances of this class are immutable to ensure thread safety and to maintain
 *   the integrity of the data.
 * - This class is a lightweight data container and does not include additional
 *   logic or validation.
 */
data class TimedPrice(
    /**
     * The reported price value.
     */
    val price: Price,

    /**
     * The time at which the price was reported.
     */
    val timestamp: Instant
)