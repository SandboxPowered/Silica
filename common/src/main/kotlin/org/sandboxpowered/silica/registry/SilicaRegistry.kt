package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.util.Identifier
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream

class SilicaRegistry<T : RegistryEntry<T>>(private val id: Identifier, override val type: Class<T>) : Registry<T> {
    var internalMap: MutableMap<Identifier, T> = HashMap()
    var registryEntries: MutableMap<Identifier, SilicaRegistryEntry<T>> = HashMap()

    fun <X : RegistryEntry<X>> cast(): Registry<X> {
        return this as Registry<X>
    }

    fun register(t: T): T {
        internalMap[t.identifier] = t
        return t
    }

    override fun iterator(): MutableIterator<T> {
        return internalMap.values.iterator()
    }

    override fun contains(id: Identifier): Boolean {
        return internalMap.containsKey(id)
    }

    override fun getId(element: T): Identifier {
        return element.identifier
    }

    override fun getUnsafe(id: Identifier): T? {
        return internalMap[id]
    }

    override fun get(id: Identifier): RegistryObject<T> {
        return registryEntries.computeIfAbsent(id) { SilicaRegistryEntry(this, it) }
    }

    override fun stream(): Stream<T> {
        return registryEntries.values.stream().filter { obj: SilicaRegistryEntry<T> -> obj.isPresent }
            .map { obj: SilicaRegistryEntry<T> -> obj.get() }
    }

    fun clearCache() {
        registryEntries.forEach { (_, entry: SilicaRegistryEntry<T>) -> entry.clearCache() }
    }

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

        override fun get(): T = internal ?: throw NullPointerException()

        override fun asOptional(): Optional<T> = Optional.ofNullable(internal)

        override fun or(supplier: RegistryObject<T>): RegistryObject<T> {
            if (!this.isPresent)
                return supplier
            return this
        }

        override fun <X : Throwable> orElseThrow(supplier: Supplier<X>): T = internal ?: throw supplier.get()

        override fun orElseGet(supplier: Supplier<T>): T = internal ?: supplier.get()
    }
}