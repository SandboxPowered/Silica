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

    val type: Class<T>
}

class RegistryDelegate<T : RegistryEntry<T>>(private val registry: Registry<T>, private val domain: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return registry[Identifier(domain, property.name.lowercase())].orNull()
    }

    val guaranteed = NonNullRegistryDelegate(registry, domain)

    class NonNullRegistryDelegate<T : RegistryEntry<T>>(private val registry: Registry<T>, private val domain: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return registry[Identifier(domain, property.name.lowercase())].get()
        }
    }
}