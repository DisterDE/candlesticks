package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.model.Candlestick
import com.disterde.candlesticks.service.HandlerOrchestrator
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val orchestrator by inject<HandlerOrchestrator>()

    routing {
        get("/candlesticks") {
            call.queryParameters["isin"]?.let { isin ->
                orchestrator.getHandler(isin)?.let { handler ->
                    call.respond(handler.getCandlesticks())
                } ?: call.respond(NotFound, "{'reason': 'not_found'}")
            } ?: call.respond(BadRequest, "{'reason': 'missing_isin'}")
        }
    }
}
