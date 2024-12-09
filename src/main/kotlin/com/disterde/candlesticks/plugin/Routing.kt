package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.exception.ApiException
import com.disterde.candlesticks.exception.HandlerNotFoundException
import com.disterde.candlesticks.service.HandlerManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Configures the routing for the candlestick service.
 *
 * ### Features:
 * - Provides an API endpoint to retrieve candlesticks for a specific ISIN.
 * - Handles errors gracefully, including missing query parameters and nonexistent handlers.
 *
 * ### Routes:
 * - **GET /candlesticks**:
 *   Retrieves the last 30 candlesticks (including the current incomplete one) for the given ISIN.
 *   - Query Parameters:
 *     - `isin`: The ISIN of the instrument (required).
 *   - Responses:
 *     - `200 OK`: Returns the candlesticks as JSON.
 *     - `400 Bad Request`: Missing or invalid ISIN parameter.
 *     - `404 Not Found`: No handler found for the specified ISIN.
 */
fun Application.configureRouting() {
    val log = KotlinLogging.logger {}
    val manager by inject<HandlerManager>()

    routing {
        get("/candlesticks") {
            call.request.queryParameters["isin"]?.let { isin ->
                val handler = manager.getHandler(isin)
                val candlesticks = handler.getCandlesticks()
                call.respond(candlesticks)
            } ?: run {
                log.warn { "Missing ISIN parameter in request" }
                call.respond(BadRequest, mapOf("reason" to "missing_isin"))
            }
        }
    }

    install(StatusPages) {
        exception<HandlerNotFoundException> { call, cause ->
            log.error(cause) { "Handler not found for ISIN: ${cause.isin}" }
            call.respond(NotFound, mapOf("reason" to "handler_not_found", "isin" to cause.isin))
        }
        exception<ApiException> { call, cause ->
            log.error(cause) { "Failed to retrieve candlesticks" }
            call.respond(InternalServerError, mapOf("reason" to "internal_server_error"))
        }
    }

    routing {
        swaggerUI(path = "swagger")
    }
}