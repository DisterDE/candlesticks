package com.disterde.candlesticks.model.event

import com.disterde.candlesticks.model.Instrument
import kotlinx.serialization.Serializable

@Serializable
data class InstrumentEvent(val type: Type, val data: Instrument) {
    enum class Type {
        ADD,
        DELETE
    }
}