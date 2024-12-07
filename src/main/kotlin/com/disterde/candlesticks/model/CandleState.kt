package com.disterde.candlesticks.model

data class CandleState(
    val closedCandles: List<Candlestick> = emptyList(),
    val currentCandle: Candlestick? = null
)