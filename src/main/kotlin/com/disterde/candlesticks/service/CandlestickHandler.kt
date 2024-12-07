package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.util.Price

interface CandlestickHandler {
    fun addPrice(price: Price)
    fun getCandlesticks(): List<Candlestick>
    fun stop()
}