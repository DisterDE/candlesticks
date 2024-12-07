package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.service.HandlerManager
import com.disterde.candlesticks.service.HandlerManagerImpl
import com.disterde.candlesticks.service.MessageListener
import com.disterde.candlesticks.service.MessageListenerImpl
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HandlerManager> { HandlerManagerImpl() }
            single<HttpClient> {
                HttpClient {
                    install(WebSockets) {
                        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
                    }
                }
            }
            single<MessageListener> { MessageListenerImpl(get(), get()) }
        })
    }
}
