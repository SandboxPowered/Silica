package org.sandboxpowered.silica.api.event

data class TypedEventResult<T>(val result: EventResult, val value: T) {
    val isCancelled: Boolean = result.isCancelled
}
