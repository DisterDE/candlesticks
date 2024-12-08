package com.disterde.candlesticks.exception

/**
 * Exception thrown when an attempt is made to access a handler for an ISIN
 * that does not have an associated handler.
 *
 * ### Parameters:
 * - `isin`: The ISIN of the instrument for which the handler was not found.
 *
 * ### Inherits:
 * - `ApiException`: This allows consistent handling of all API-related exceptions.
 *
 * ### Usage:
 * This exception is typically used in the `HandlerManager` implementation
 * when attempting to access a handler for an unknown instrument.
 *
 * ### Example:
 * ```kotlin
 * throw HandlerNotFoundException("US0378331005")
 * ```
 *
 * The error message will be: "No handler found for the specified ISIN. ISIN: US0378331005"
 */
class HandlerNotFoundException(isin: String) : ApiException(
    "No handler found for the specified ISIN. ISIN: $isin"
)