package com.disterde.candlesticks.model.event

import com.disterde.candlesticks.model.Quote
import kotlinx.serialization.Serializable

@Serializable
data class QuoteEvent(val data: Quote)