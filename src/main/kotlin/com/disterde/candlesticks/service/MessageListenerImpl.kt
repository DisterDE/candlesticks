package com.disterde.candlesticks.service

import com.disterde.candlesticks.model.event.InstrumentEvent
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.ADD
import com.disterde.candlesticks.model.event.InstrumentEvent.Type.DELETE
import com.disterde.candlesticks.model.event.QuoteEvent
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessageListenerImpl(
    private val client: HttpClient,
    private val orchestrator: HandlerOrchestrator
) : MessageListener {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var active = true

    override fun start() {
        scope.launch {
            while (active) {
                try {
                    client.webSocket(host = HOST, port = PORT, path = QUOTES) {
                        while (active) {
                            val quote = receiveDeserialized<QuoteEvent>().data
                            scope.launch { orchestrator.getHandler(quote.isin)?.addPrice(quote.price) }
                        }
                    }
                } catch (e: Exception) {
                    println(e.message)
                    delay(1_000)
                }
            }
        }

        scope.launch {
            while (active) {
                try {
                    client.webSocket(host = HOST, port = PORT, path = INSTRUMENTS) {
                        while (active) {
                            val (type, data) = receiveDeserialized<InstrumentEvent>()
                            scope.launch {
                                when (type) {
                                    ADD -> orchestrator.createHandler(data.isin)
                                    DELETE -> orchestrator.deleteHandler(data.isin)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println(e.message)
                    delay(1_000)
                }
            }
        }
    }

    override fun stop() {
        active = false
    }

    companion object {
        private val HOST = "localhost"
        private val PORT = 8032
        private val QUOTES = "/quotes"
        private val INSTRUMENTS = "/instruments"
    }
}