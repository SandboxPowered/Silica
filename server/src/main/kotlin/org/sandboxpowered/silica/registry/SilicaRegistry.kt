package org.sandboxpowered.silica.registry

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryDelegate
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.registry.RegistryObject
import org.sandboxpowered.silica.api.Identifier
import java.util.*
import java.util.function.Supplier

class SilicaRegistry<T : RegistryEntry<T>>(private val id: Identifier, override val type: Class<T>) : Registry<T> {
    var internalMap: MutableMap<Identifier, T> = HashMap()
    var registryEntries: MutableMap<Identifier, SilicaRegistryEntry<T>> = HashMap()
    var listeners: MutableList<(T) -> Unit> = ArrayList()
    override val values: Map<Identifier, T>
        get() = ImmutableMap.copyOf(internalMap)

    @Suppress("UNCHECKED_CAST")
    fun <X : RegistryEntry<X>> cast(): Registry<X> = this as Registry<X>

    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    override fun register(value: T): T {
        internalMap[value.identifier] = value
        listeners.forEach { it(value) }
        return value
    }

    override fun iterator() = internalMap.values.iterator()

    override fun contains(id: Identifier) = internalMap.containsKey(id)

    override fun getId(element: T) = element.identifier

    override fun getUnsafe(id: Identifier) = internalMap[id]

    override fun get(id: Identifier) =
        registryEntries.computeIfAbsent(id) { SilicaRegistryEntry(this, it) }

    override fun stream() =
        registryEntries.values.stream().filter { obj: SilicaRegistryEntry<T> -> obj.isPresent }
            .map { obj: SilicaRegistryEntry<T> -> obj.get() }

    fun clearCache() {
        registryEntries.forEach { (_, entry: SilicaRegistryEntry<T>) -> entry.clearCache() }
    }

    private val delegates = HashMap<String, RegistryDelegate<T>>()

    override fun invoke(domain: String): RegistryDelegate<T> =
        delegates.computeIfAbsent(domain) { RegistryDelegate(this, it) }

    override fun invoke(domain: String, optional: Boolean): RegistryDelegate.NullableRegistryDelegate<T> =
        invoke(domain).optional

    class SilicaRegistryEntry<T : RegistryEntry<T>>(
        override val registry: SilicaRegistry<T>,
        override val id: Identifier
    ) : RegistryObject<T> {
        private var hasCached = false
        private var cachedValue: T? = null

        private fun updateCache() {
            if (!hasCached) {
                cachedValue = registry.internalMap[id]
                hasCached = true
            }
        }

        fun clearCache() {
            cachedValue = null
            hasCached = false
        }

        private val internal: T?
            get() {
                updateCache()
                return cachedValue
            }
        override val isPresent: Boolean
            get() {
                updateCache()
                return cachedValue != null
            }
        override val isEmpty: Boolean
            get() = !isPresent

        override fun get() = internal ?: throw NoSuchElementException()

        override fun asOptional() = Optional.ofNullable(internal)

        override fun or(supplier: RegistryObject<T>) = if (!this.isPresent) supplier else this

        override fun <X : Throwable> orElseThrow(supplier: Supplier<X>) = internal ?: throw supplier.get()

        override fun orElseGet(supplier: Supplier<T>) = internal ?: supplier.get()

        override fun orNull(): T? = internal
    }
}