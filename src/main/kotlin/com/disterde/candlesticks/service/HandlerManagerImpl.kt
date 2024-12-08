package com.disterde.candlesticks.service

import com.disterde.candlesticks.exception.HandlerExistsException
import com.disterde.candlesticks.exception.HandlerNotFoundException
import com.disterde.candlesticks.util.ISIN
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager for handling candlestick handlers for multiple financial instruments.
 *
 * This class provides methods to manage the lifecycle of `CandlestickHandler` instances
 * for specific instruments, identified by their ISIN. It ensures that each instrument
 * has a single dedicated handler and supports operations such as retrieval, creation,
 * and deletion of handlers.
 *
 * ### Features:
 * - Thread-safe operations using `ConcurrentHashMap` for managing handlers.
 * - Automatic stopping of handlers upon deletion to free resources.
 * - Logging for lifecycle events to aid in debugging and monitoring.
 *
 * ### Architecture:
 * - Handlers are created on-demand for each unique ISIN, ensuring separation of concerns
 *   and scalability for handling multiple instruments concurrently.
 * - The design uses a `ConcurrentHashMap` for thread-safe access, allowing simultaneous
 *   operations across multiple instruments without contention.
 *
 * ### Scalability Considerations:
 * The current implementation is optimized for a moderate number of instruments.
 * To handle thousands or millions of instruments, the following adjustments could be made:
 * 1. **Distributed Management:**
 *    - Shard instruments across multiple nodes by ISIN hash.
 *    - Use a distributed storage system (e.g., Redis) for global state management.
 * 2. **Load-Based Scaling:**
 *    - Dynamically allocate resources for handlers based on the number of updates.
 *    - Introduce a pool of reusable handler instances to reduce overhead.
 * 3. **Failover and Redundancy:**
 *    - Persist handler state to a durable store (e.g., database) for recovery after failures.
 *    - Use leader election algorithms to manage distributed failover scenarios.
 */
class HandlerManagerImpl : HandlerManager {

    private val map = ConcurrentHashMap<ISIN, CandlestickHandler>()
    private val log = KotlinLogging.logger {}

    /**
     * Retrieves the handler for a given ISIN.
     *
     * @param isin The ISIN of the instrument.
     * @return The corresponding `CandlestickHandler` instance.
     * @throws HandlerNotFoundException if no handler exists for the given ISIN.
     */
    override fun getHandler(isin: ISIN): CandlestickHandler {
        return map[isin] ?: throw HandlerNotFoundException(isin).also {
            log.warn { "Attempted to access non-existent handler for ISIN: $isin" }
        }
    }

    /**
     * Creates a new handler for the given ISIN.
     *
     * If a handler for the ISIN already exists, an exception is thrown.
     *
     * @param isin The ISIN of the instrument.
     * @throws HandlerExistsException if a handler for the ISIN already exists.
     */
    override fun createHandler(isin: ISIN) {
        val existingHandler = map.putIfAbsent(isin, CandlestickHandlerImpl(isin, MAX_CANDLES))
        if (existingHandler != null) {
            log.error { "Failed to add handler for ISIN $isin: already exists" }
            throw HandlerExistsException(isin)
        }
        log.info { "Created handler for ISIN: $isin" }
    }

    /**
     * Deletes the handler for the given ISIN.
     *
     * If the handler exists, it is stopped and removed. If no handler exists, an exception is thrown.
     *
     * @param isin The ISIN of the instrument.
     * @throws HandlerNotFoundException if no handler exists for the given ISIN.
     */
    override fun deleteHandler(isin: ISIN) {
        val removedHandler = map.remove(isin)
        if (removedHandler != null) {
            removedHandler.stop()
            log.info { "Deleted handler for ISIN: $isin" }
        } else {
            log.error { "Failed to delete handler for ISIN $isin: not found" }
            throw HandlerNotFoundException(isin)
        }
    }

    companion object {
        private const val MAX_CANDLES = 30
    }
}