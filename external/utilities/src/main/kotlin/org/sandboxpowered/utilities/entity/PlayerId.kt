package org.sandboxpowered.silica.api.entity

import java.util.*

@JvmInline
value class PlayerId(val uuid: UUID) {
    constructor(string: String) : this(UUID.fromString(string))

    override fun toString(): String = uuid.toString()
}