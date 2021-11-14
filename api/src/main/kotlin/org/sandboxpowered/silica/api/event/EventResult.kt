package org.sandboxpowered.silica.api.event

enum class EventResult {
    DEFAULT,
    ALLOW,
    DENY;

    val isCancelled: Boolean
        get() = this == DENY
}