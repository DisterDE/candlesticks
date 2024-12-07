package com.disterde.candlesticks.service

import com.disterde.candlesticks.util.ISIN

interface HandlerOrchestrator {
    fun getHandler(isin: ISIN): CandlestickHandler?
    fun createHandler(isin: ISIN)
    fun deleteHandler(isin: ISIN)
}
