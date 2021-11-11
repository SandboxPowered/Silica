package org.sandboxpowered.silica.api.internal

import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import java.util.*
import kotlin.reflect.KClass

object SilicaAPI : InternalAPI by getInstance()

fun getInstance(): InternalAPI {
    val first = ServiceLoader.load(InternalAPI::class.java).findFirst()
    require(first.isPresent) { "Unable to find Internal API instance." }
    return first.get()
}

interface InternalAPI {
    fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>): Registry<T>
}