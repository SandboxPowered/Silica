package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.util.Identifier

interface RegistryEntry<T : RegistryEntry<T>> : Comparable<T> {
    val identifier: Identifier
    val registry: Registry<T>

    fun hasTag(tag: Identifier) = registry[identifier].hasTag(tag)

    override fun compareTo(other: T): Int = identifier.compareTo(other.identifier)
}