package com.disterde.candlesticks.plugin

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Configures JSON serialization for the Ktor application.
 *
 * ### Features:
 * - Integrates `kotlinx.serialization` for JSON support.
 * - Configures the application to:
 *   - Ignore unknown keys in incoming JSON payloads.
 *   - Format outgoing JSON with pretty-print for readability.
 *
 * ### Why this configuration?
 * - **Ignore unknown keys:** Prevents errors when incoming payloads contain additional fields.
 * - **Pretty-print:** Useful for debugging and enhancing the readability of responses in development.
 *
 * ### Example:
 * ```kotlin
 * application {
 *     configureSerialization()
 * }
 * ```
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Ignore extra fields in incoming JSON
            prettyPrint = true       // Format outgoing JSON for better readability
        })
    }
}