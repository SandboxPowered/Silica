package org.sandboxpowered.silica.api.internal

import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry

object SilicaAPI : InternalAPI by null!! {

}

interface InternalAPI {
    fun <T : RegistryEntry<T>> getRegistry(): Registry<T>
}