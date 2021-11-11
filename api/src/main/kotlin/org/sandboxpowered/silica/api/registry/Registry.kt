package org.sandboxpowered.silica.api.registry

import org.jetbrains.annotations.ApiStatus
import org.sandboxpowered.silica.api.util.Identifier
import java.util.stream.Stream
import kotlin.reflect.KProperty

interface Registry<T : RegistryEntry<T>> : Iterable<T> {
    fun stream(): Stream<T>
    operator fun contains(id: Identifier): Boolean
    operator fun get(id: Identifier): RegistryObject<T>
    fun getId(element: T): Identifier

    @ApiStatus.Experimental
    fun getUnsafe(id: Identifier): T?

    fun register(value: T): T

    val type: Class<T>
}

class RegistryDelegate<T : RegistryEntry<T>>(private val registry: Registry<T>, private val domain: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        registry[Identifier(domain, property.name.lowercase())].get()

    val optional = NullableRegistryDelegate(registry, domain)

    class NullableRegistryDelegate<T : RegistryEntry<T>>(
        private val registry: Registry<T>,
        private val domain: String
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? =
            registry[Identifier(domain, property.name.lowercase())].orNull()
    }
}