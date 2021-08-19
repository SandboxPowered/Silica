package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.util.Identifier
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream

class SilicaRegistry<T : RegistryEntry<T>>(private val registryId: Identifier, override val registryType: Class<T>) :
    Registry<T> {
    var internalMap: MutableMap<Identifier, T> = HashMap()
    var registryEntries: MutableMap<Identifier, SilicaRegistryEntry<T>> = HashMap()

    fun <X : RegistryEntry<X>> cast(): Registry<X> {
        return this as Registry<X>
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

    override fun get(identity: Identifier): RegistryObject<T> {
        return registryEntries.computeIfAbsent(identity) { id -> SilicaRegistryEntry(this, id) }
    }

    override fun stream(): Stream<T> {
        return registryEntries.values.stream().filter { obj: SilicaRegistryEntry<T> -> obj.isPresent }
            .map { obj: SilicaRegistryEntry<T> -> obj.get() }
    }

    fun clearCache() {
        registryEntries.forEach { (_, aEntry: SilicaRegistryEntry<T>) -> aEntry.clearCache() }
    }

    class SilicaRegistryEntry<T : RegistryEntry<T>>(
        private val registry: SilicaRegistry<T>,
        private val target: Identifier
    ) : RegistryObject<T> {
        private var hasCached = false
        private var cachedValue: T? = null

        private fun updateCache() {
            if (!hasCached) {
                cachedValue = registry.internalMap[target]
                hasCached = true
            }
        }

        fun clearCache() {
            cachedValue = null
            hasCached = false
        }

        val internal: T?
            get() {
                updateCache()
                return cachedValue
            }

        override fun get(): T {
            return internal ?: throw NullPointerException()
        }

        override fun asOptional(): Optional<T> {
            return Optional.ofNullable(internal)
        }

        override fun isEmpty(): Boolean = !isPresent

        override fun or(supplier: RegistryObject<T>): RegistryObject<T> {
            if (!this.isPresent)
                return supplier
            return this
        }

        override fun getId(): Identifier = target

        override fun <X : Throwable?> orElseThrow(supplier: Supplier<X>): T {
            TODO("Not yet implemented")
        }

        override fun getRegistry(): Registry<T> = registry

        override fun orElseGet(other: Supplier<T>): T = internal ?: other.get()

        override fun isPresent(): Boolean {
            updateCache()
            return cachedValue != null
        }
    }
}