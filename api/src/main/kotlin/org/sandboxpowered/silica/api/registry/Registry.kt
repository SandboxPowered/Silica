package org.sandboxpowered.silica.api.registry

import org.jetbrains.annotations.ApiStatus.Experimental
import org.jetbrains.annotations.ApiStatus.Internal
import org.sandboxpowered.silica.api.util.Identifier
import java.util.stream.Stream
import kotlin.reflect.KProperty

interface Registry<T : RegistryEntry<T>> : Iterable<T> {
    fun stream(): Stream<T>
    operator fun contains(id: Identifier): Boolean
    operator fun get(id: Identifier): RegistryObject<T>
    fun getId(element: T): Identifier

    @Experimental
    fun getUnsafe(id: Identifier): T?

    @Internal
    fun register(value: T): T

    @Internal
    fun registerAll(values: Collection<T>)

    @Internal
    fun registerTags(values: Map<Identifier, Iterable<Identifier>>)

    val values: Map<Identifier, T>
    val type: Class<T>

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        this[Identifier("minecraft", property.name.lowercase())].get()

    operator fun invoke(domain: String = "minecraft"): RegistryDelegate<T>
    operator fun invoke(domain: String = "minecraft", optional: Boolean): RegistryDelegate.NullableRegistryDelegate<T>

    /**
     * Nullable to separate uninitialized/missing tag from tag with no objects
     */
    fun getByTag(tag: Identifier): Set<RegistryObject<T>>?
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