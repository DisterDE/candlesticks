package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.ISIN
import kotlinx.serialization.Serializable

/**
 * Represents a financial instrument that can be traded.
 *
 * This class contains essential metadata about a financial instrument, including:
 * - Its unique identifier (`ISIN`).
 * - A human-readable description of the instrument.
 *
 * ### Fields:
 * - `isin`: The International Securities Identification Number (ISIN), which uniquely identifies the instrument.
 * - `description`: A textual description of the instrument, useful for display purposes.
 *
 * ### Usage:
 * - This class is serialized/deserialized using `kotlinx.serialization`.
 * - It is used in systems that manage instrument catalogs, such as adding or deleting instruments.
 *
 * ### Examples:
 * ```kotlin
 * val instrument = Instrument(
 *     isin = "US0378331005",
 *     description = "Apple Inc. Common Stock"
 * )
 * ```
 */
@Serializable
data class Instrument(
    /**
     * The unique International Securities Identification Number (ISIN) for the instrument.
     */
    val isin: ISIN,

    /**
     * A human-readable description of the instrument.
     */
    val description: String
)