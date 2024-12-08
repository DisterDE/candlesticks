package com.disterde.candlesticks.exception

/**
 * Base class for all API-related exceptions in the candlestick service.
 *
 * ### Purpose:
 * - This class serves as the parent for all exceptions specific to the API layer,
 *   providing a consistent hierarchy for error handling.
 * - By extending `RuntimeException`, it allows for unchecked exceptions
 *   that can propagate without mandatory try-catch blocks.
 *
 * ### Parameters:
 * - `message`: A descriptive error message detailing the nature of the exception.
 *
 * ### Usage:
 * Subclasses of `ApiException` represent specific errors, such as missing handlers
 * (`HandlerNotFoundException`) or attempting to create duplicate handlers (`HandlerExistsException`).
 *
 * ### Example:
 * ```kotlin
 * throw ApiException("Generic API error")
 * ```
 */
open class ApiException(message: String) : RuntimeException(message)