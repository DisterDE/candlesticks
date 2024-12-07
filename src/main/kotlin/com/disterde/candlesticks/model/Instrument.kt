package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.ISIN
import kotlinx.serialization.Serializable

@Serializable
data class Instrument(val isin: ISIN, val description: String)

