package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.ISIN
import com.disterde.candlesticks.util.Price
import kotlinx.serialization.Serializable

@Serializable
data class Quote(val isin: ISIN, val price: Price)