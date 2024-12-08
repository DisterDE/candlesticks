package com.disterde.candlesticks.model.event

import com.disterde.candlesticks.model.Instrument
import kotlinx.serialization.Serializable

/**
 * Represents an event related to a financial instrument.
 *
 * These events are typically received from the `/instruments` WebSocket stream and
 * indicate changes in the set of tracked instruments, such as additions or deletions.
 *
 * ### Fields:
 * - `type`: The type of the event, indicating whether an instrument was added or deleted.
 * - `data`: The `Instrument` associated with the event.
 *
 * ### Event Types:
 * - `ADD`: Indicates that a new instrument has been added to the system.
 * - `DELETE`: Indicates that an instrument has been removed from the system.
 *
 * ### Usage:
 * - This class is serialized/deserialized using `kotlinx.serialization`.
 * - It is used for handling instrument management in the `HandlerManager` or equivalent classes.
 */
@Serializable
data class InstrumentEvent(
    /**
     * The type of the instrument event.
     */
    val type: Type,

    /**
     * The financial instrument associated with this event.
     */
    val data: Instrument
) {
    /**
     * Enum representing the possible types of instrument events.
     */
    enum class Type {
        /**
         * Indicates that a new instrument has been added.
         */
        ADD,

        /**
         * Indicates that an instrument has been removed.
         */
        DELETE
    }
}