package org.sandboxpowered.silica.loading

import org.sandboxpowered.api.addon.Log
import org.sandboxpowered.api.addon.SandboxAPI
import org.sandboxpowered.api.addon.service.CreationService
import org.sandboxpowered.api.registry.DeferredRegistrar
import org.sandboxpowered.api.registry.Registry
import org.sandboxpowered.api.registry.RegistryEntry
import java.util.*
import java.util.function.Consumer

class AddonSpecificAPIReference(private val loader: SandboxLoader) : SandboxAPI {
    override fun getLog(): Log {
        TODO("Not yet implemented")
    }

    override fun <T : RegistryEntry<T>> getRegistrar(registry: Registry<T>): DeferredRegistrar<T> {
        TODO("Not yet implemented")
    }

    override fun <T : CreationService> getCreationService(tClass: Class<T>): Optional<T> {
        return Optional.empty()
    }

    override fun <T : CreationService> useCreationService(resourceServiceClass: Class<T>, consumer: Consumer<T>) {
        getCreationService(resourceServiceClass).ifPresent(consumer)
    }
}