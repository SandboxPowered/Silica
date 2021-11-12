package org.sandboxpowered.silica.api.event

interface Event<T> {
    // Nullable so if no subscriber is registered, the event can be ignored.
    val invoker: T?

    fun subscribe(listener: T)
}