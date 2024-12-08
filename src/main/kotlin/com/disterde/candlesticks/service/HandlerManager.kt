package com.disterde.candlesticks.service

import com.disterde.candlesticks.util.ISIN

/**
 * Interface for managing candlestick handlers for multiple financial instruments.
 *
 * This interface defines the lifecycle management of `CandlestickHandler` instances,
 * allowing for creation, retrieval, and deletion of handlers associated with specific ISINs.
 *
 * ### Responsibilities:
 * - Ensures that each ISIN has a dedicated handler for processing candlestick data.
 * - Provides a mechanism to safely manage the lifecycle of handlers.
 * - Handles concurrency and thread-safe operations at the implementation level.
 *
 * ### Usage:
 * - Call `createHandler` to add a handler for a new instrument.
 * - Use `getHandler` to retrieve an existing handler by its ISIN.
 * - Use `deleteHandler` to remove a handler when it is no longer needed.
 */
interface HandlerManager {

    /**
     * Retrieves the candlestick handler for a specific ISIN.
     *
     * This function allows access to the `CandlestickHandler` responsible for managing
     * candlestick data for the given ISIN. If no handler exists, the implementation
     * may throw an exception or return `null`, depending on the specific behavior.
     *
     * @param isin The ISIN (International Securities Identification Number) of the instrument.
     * @return The `CandlestickHandler` for the specified ISIN, or `null` if not found.
     */
    fun getHandler(isin: ISIN): CandlestickHandler

    /**
     * Creates a new candlestick handler for a specific ISIN.
     *
     * This function initializes a new `CandlestickHandler` for the given ISIN.
     * If a handler for the ISIN already exists, the implementation should throw an exception.
     *
     * @param isin The ISIN of the instrument.
     * @throws HandlerExistsException if a handler for the given ISIN already exists.
     */
    fun createHandler(isin: ISIN)

    /**
     * Deletes the candlestick handler for a specific ISIN.
     *
     * This function removes the `CandlestickHandler` associated with the given ISIN.
     * The implementation should ensure that the handler is properly stopped and any
     * associated resources are released. If no handler exists for the ISIN, an exception
     * should be thrown.
     *
     * @param isin The ISIN of the instrument.
     * @throws HandlerNotFoundException if no handler exists for the given ISIN.
     */
    fun deleteHandler(isin: ISIN)
}