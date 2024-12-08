package com.disterde.candlesticks.service

/**
 * Interface for listening to messages within a system.
 *
 * The `MessageListener` interface provides a contract for starting and stopping
 * the message listening process. Implementations of this interface are responsible
 * for handling the logic required to listen to and process incoming messages.
 *
 * Responsibilities:
 * - Start listening to messages.
 * - Stop listening to messages and perform any necessary cleanup.
 *
 * Implementations should ensure that the start and stop functions can be called safely
 * in any lifecycle state and handle any required synchronization or resource management.
 */
interface MessageListener {
    fun start()
    fun stop()
}