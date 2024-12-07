package com.disterde.candlesticks

import com.disterde.candlesticks.plugin.configureKoin
import com.disterde.candlesticks.plugin.configureMonitoring
import com.disterde.candlesticks.plugin.configureRouting
import com.disterde.candlesticks.plugin.configureSerialization
import com.disterde.candlesticks.service.MessageListener
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.ext.inject

fun main() {
    embeddedServer(
        CIO,
        port = 9000,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureMonitoring()
    configureRouting()
    configureStreams()
    configureSerialization()
}

fun Application.configureStreams() {
    val listener by inject<MessageListener>()

    listener.start()
}
