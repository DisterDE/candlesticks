package com.disterde.candlesticks.service

import com.disterde.candlesticks.util.ISIN
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

class HandlerManagerImpl : HandlerManager {

    private val map = ConcurrentHashMap<ISIN, CandlestickHandler>()
    private val log = KotlinLogging.logger {}

    override fun getHandler(isin: ISIN): CandlestickHandler? {
        return map[isin]/* ?: throw HandlerNotFoundException(isin)*/
    }

    override fun createHandler(isin: ISIN) {
        val existingHandler = map.putIfAbsent(isin, CandlestickHandlerImpl(isin, MAX_CANDLES))
        log.info { "Added handler: $isin" }
//        if (existingHandler != null) throw HandlerExistsException(isin)
    }

    override fun deleteHandler(isin: ISIN) {
        log.info { "Removed handler: $isin" }
        map.remove(isin)?.also { it.stop() }/* ?: throw HandlerNotFoundException(isin)*/
    }

    companion object {
        private const val MAX_CANDLES = 30
    }
}