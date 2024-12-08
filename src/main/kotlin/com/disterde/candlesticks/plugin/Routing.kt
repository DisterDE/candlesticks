package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.exception.ApiException
import com.disterde.candlesticks.exception.HandlerNotFoundException
import com.disterde.candlesticks.service.HandlerManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
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
    val orchestrator by inject<HandlerManager>()

    routing {
        /**
         * GET /candlesticks
         * Retrieves the candlestick data for a specified ISIN.
         */
        get("/candlesticks") {
            call.request.queryParameters["isin"]?.let { isin ->
                try {
                    val handler = orchestrator.getHandler(isin)
                    val candlesticks = handler.getCandlesticks()
                    call.respond(candlesticks)
                } catch (e: HandlerNotFoundException) {
                    log.error(e) { "Handler not found for ISIN: $isin" }
                    call.respond(NotFound, mapOf("reason" to "handler_not_found", "isin" to isin))
                } catch (e: Exception) {
                    log.error(e) { "Failed to retrieve candlesticks for ISIN: $isin" }
                    call.respond(InternalServerError, mapOf("reason" to "internal_server_error"))
                }
            } ?: run {
                log.warn { "Missing ISIN parameter in request" }
                call.respond(BadRequest, mapOf("reason" to "missing_isin"))
            }
        }
    }
}