package com.disterde.candlesticks.plugin

import com.disterde.candlesticks.service.HandlerManager
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val orchestrator by inject<HandlerManager>()

    routing {
        get("/candlesticks") {
            call.queryParameters["isin"]?.let { isin ->
                orchestrator.getHandler(isin)?.let { handler ->
                    call.respond(handler.getCandlesticks())
                } ?: call.respond(NotFound, "{'reason': 'report_not_found'}")
            } ?: call.respond(BadRequest, "{'reason': 'missing_isin'}")
        }
    }
}
