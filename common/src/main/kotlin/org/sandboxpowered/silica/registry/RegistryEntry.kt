package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.util.Identifier

interface RegistryEntry<T : RegistryEntry<T>> : Comparable<T> {
    val identifier: Identifier
    val registry: Registry<T>

    override fun compareTo(other: T): Int = identifier.compareTo(other.identifier)
}