package org.sandboxpowered.silica.api.nbt

sealed interface NBT {
    fun asString(): String
}