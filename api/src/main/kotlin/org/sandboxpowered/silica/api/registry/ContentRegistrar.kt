package org.sandboxpowered.silica.api.registry

interface ContentRegistrar<T : RegistryEntry<T>> {
    fun register(entry: T): RegistryObject<T>
}