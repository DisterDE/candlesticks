package com.disterde.candlesticks.plugin

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.plugins.*

/**
 * Configures logging for incoming HTTP calls.
 *
 * ### Features:
 * - Logs information about incoming requests, such as paths and methods.
 * - Filters requests based on their path to avoid unnecessary logging.
 * - Sets the logging level to `INFO` for standard requests.
 *
 * ### Configuration:
 * - Uses the `CallLogging` plugin to intercept and log HTTP requests.
 * - By default, logs all requests starting with `/` (can be customized via the filter).
 *
 * ### Usage:
 * This function should be called in the `Application.module()` during application initialization:
 * ```kotlin
 * fun Application.module() {
 *     configureMonitoring()
 * }
 * ```
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        // Sets the logging level to INFO
        level = Level.DEBUG

        // Filters the requests to be logged (e.g., log only paths starting with '/')
        filter { call ->
            val path = call.request.path()
            path.startsWith("/")
        }

        // Customizes the log message for each call
        format { call ->
            val request = call.request
            "HTTP ${request.httpMethod.value} - ${request.path()} from ${request.origin.remoteHost}"
        }
    }
}