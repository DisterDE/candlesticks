package com.disterde.candlesticks

import com.disterde.candlesticks.plugin.configureKoin
import com.disterde.candlesticks.plugin.configureMonitoring
import com.disterde.candlesticks.plugin.configureRouting
import com.disterde.candlesticks.plugin.configureSerialization
import com.disterde.candlesticks.service.MessageListener
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}

/**
 * Entry point of the candlestick service application.
 *
 * This application is built using the Ktor framework and provides:
 * - WebSocket listeners for real-time instrument and quote updates.
 * - RESTful endpoints for querying candlestick data.
 * - Dependency injection via Koin.
 */
fun main() {
    log.info { "Starting the Candlestick Service..." }
    embeddedServer(
        CIO,
        port = 9000,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

/**
 * Configures the application modules.
 *
 * This method integrates various components of the application, such as:
 * - Dependency injection via Koin.
 * - Monitoring and metrics setup.
 * - Routing configuration for RESTful APIs.
 * - Serialization for request and response handling.
 * - Stream handling for real-time WebSocket connections.
 */
fun Application.module() {
    configureKoin() // Configures dependency injection
    configureMonitoring() // Sets up monitoring tools (e.g., metrics, logging)
    configureRouting() // Sets up HTTP routing for REST APIs
    configureStreams() // Starts WebSocket listeners for real-time data
    configureSerialization() // Configures JSON serialization/deserialization
}

/**
 * Configures the WebSocket streams for real-time data.
 *
 * This method retrieves the `MessageListener` instance via Koin dependency injection
 * and starts it to begin processing WebSocket events.
 */
fun Application.configureStreams() {
    val listener by inject<MessageListener>()
    listener.start()
}