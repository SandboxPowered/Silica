package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.util.Identifier

interface RegistryEntry<T : RegistryEntry<T>> {
    val identifier: Identifier
    val registry: Registry<T>
}