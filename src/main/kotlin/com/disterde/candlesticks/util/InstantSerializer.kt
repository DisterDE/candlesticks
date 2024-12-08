package com.disterde.candlesticks.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * A custom serializer for `java.time.Instant` to enable serialization and deserialization
 * using `kotlinx.serialization`.
 *
 * ### Overview:
 * - This serializer converts `Instant` to and from an ISO-8601 formatted string.
 * - It ensures compatibility with systems or APIs that represent timestamps as strings in this format.
 *
 * ### Usage:
 * - Annotate any `Instant` field with `@Serializable(with = InstantSerializer::class)`.
 *
 * ### Example:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     @Serializable(with = InstantSerializer::class)
 *     val timestamp: Instant
 * )
 * ```
 */
object InstantSerializer : KSerializer<Instant> {

    /**
     * The descriptor defining the type and format of the serialized data.
     *
     * - PrimitiveKind.STRING indicates that the serialized format is a string.
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    /**
     * Serializes an `Instant` into an ISO-8601 formatted string.
     *
     * @param encoder The encoder used to write the serialized data.
     * @param value The `Instant` to serialize.
     */
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Converts Instant to ISO-8601 string.
    }

    /**
     * Deserializes an ISO-8601 formatted string into an `Instant`.
     *
     * @param decoder The decoder used to read the serialized data.
     * @return The parsed `Instant` object.
     */
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString()) // Parses ISO-8601 string into Instant.
    }
}