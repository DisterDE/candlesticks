package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.InstantSerializer
import com.disterde.candlesticks.util.Price
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Candlestick(
    @Serializable(with = InstantSerializer::class)
    val openTimestamp: Instant,
    @Serializable(with = InstantSerializer::class)
    val closeTimestamp: Instant,
    val openPrice: Price,
    val highPrice: Price,
    val lowPrice: Price,
    val closingPrice: Price
)

