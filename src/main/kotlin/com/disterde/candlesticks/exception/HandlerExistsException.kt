package com.disterde.candlesticks.exception

/**
 * Exception thrown when an attempt is made to create a handler for an ISIN
 * that already has an existing handler.
 *
 * ### Parameters:
 * - `isin`: The ISIN of the instrument for which the handler already exists.
 *
 * ### Inherits:
 * - `ApiException`: This allows consistent handling of all API-related exceptions.
 *
 * ### Usage:
 * This exception is typically used in the `HandlerManager` implementation
 * to prevent duplication of handlers for the same instrument.
 *
 * ### Example:
 * ```kotlin
 * throw HandlerExistsException("US0378331005")
 * ```
 *
 * The error message will be: "Handler already exists for the specified ISIN: US0378331005"
 */
class HandlerExistsException(isin: String) : ApiException(
    "Handler already exists for the specified ISIN: $isin"
)