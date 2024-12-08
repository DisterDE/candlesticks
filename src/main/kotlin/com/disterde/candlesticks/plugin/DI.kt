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

/**
 * Configures the Koin dependency injection framework for the application.
 *
 * ### Features:
 * - Registers dependencies such as `HandlerManager`, `HttpClient`, and `MessageListener`.
 * - Enables logging for Koin using SLF4J.
 * - Integrates Koin with the Ktor application lifecycle.
 *
 * ### Registered Dependencies:
 * - `HandlerManager`: Manages handlers for ISINs.
 * - `HttpClient`: Configured with WebSocket support and JSON serialization for real-time communication.
 * - `MessageListener`: Listens to WebSocket streams and dispatches events to the `HandlerManager`.
 *
 * ### Usage:
 * This function should be called in the `Application.module()`:
 * ```kotlin
 * fun Application.module() {
 *     configureKoin()
 * }
 * ```
 */
fun Application.configureKoin() {
    install(Koin) {
        // Enable SLF4J logging for Koin
        slf4jLogger()

        // Define and load application modules
        modules(
            module {
                // Register a singleton HandlerManager implementation
                single<HandlerManager> { HandlerManagerImpl() }

                // Register a configured HttpClient with WebSocket and JSON support
                single<HttpClient> {
                    HttpClient {
                        install(WebSockets) {
                            contentConverter = KotlinxWebsocketSerializationConverter(
                                Json { ignoreUnknownKeys = true }
                            )
                        }
                    }
                }

                // Register a MessageListener implementation
                single<MessageListener> { MessageListenerImpl(get(), get()) }
            }
        )
    }
}