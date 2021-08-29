package org.sandboxpowered.silica.nbt

sealed interface NBT {
    fun asString(): String
}