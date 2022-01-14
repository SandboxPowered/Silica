package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.utilities.Identifier

interface RegistryEntry<T : RegistryEntry<T>> : Comparable<T> {
    val identifier: Identifier
    val registry: Registry<T>

    override fun compareTo(other: T): Int = identifier.compareTo(other.identifier)
}