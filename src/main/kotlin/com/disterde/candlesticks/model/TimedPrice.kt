package com.disterde.candlesticks.model

import com.disterde.candlesticks.util.Price
import java.time.Instant

data class TimedPrice(val price: Price, val timestamp: Instant)