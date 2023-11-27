package org.sandboxpowered.silica.registry

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryDelegate
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.registry.RegistryObject
import org.sandboxpowered.silica.api.util.Identifier
import java.util.*
import java.util.function.Supplier
import java.util.stream.Stream

class SilicaRegistry<T : RegistryEntry<T>>(private val id: Identifier, override val type: Class<T>) : Registry<T> {
    private val internalMap: MutableMap<Identifier, T> = Object2ObjectOpenHashMap()
    private val registryEntries: MutableMap<Identifier, SilicaRegistryEntry<T>> = Object2ObjectOpenHashMap()
    private val internalTags: MutableMap<Identifier, Set<RegistryObject<T>>> = Object2ObjectOpenHashMap()
    private val listeners: MutableList<(T) -> Unit> = LinkedList()

    override val values: Map<Identifier, T>
        get() = ImmutableMap.copyOf(internalMap)

    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    override fun register(value: T): T {
        internalMap[value.identifier] = value
        listeners.forEach { it(value) }
        return value
    }

    override fun registerAll(values: Collection<T>) {
        internalMap.putAll(values.associateBy(RegistryEntry<*>::identifier))
    }

    override fun iterator() = internalMap.values.iterator()

    override fun contains(id: Identifier) = internalMap.containsKey(id)

    override fun getId(element: T) = element.identifier

    override fun getUnsafe(id: Identifier) = internalMap[id]

    override fun get(id: Identifier): RegistryObject<T> = getPrivately(id)

    private fun getPrivately(id: Identifier) = registryEntries.computeIfAbsent(id) { SilicaRegistryEntry(this, it) }

    override fun stream(): Stream<T> =
        registryEntries.values.stream()
            .filter(RegistryObject<T>::isPresent)
            .map(RegistryObject<T>::get)

    fun clearCache() {
        registryEntries.forEach { (_, entry) -> entry.clearCache() }
    }

    private val delegates = HashMap<String, RegistryDelegate<T>>()

    override fun invoke(domain: String): RegistryDelegate<T> =
        delegates.computeIfAbsent(domain) { RegistryDelegate(this, it) }

    override fun invoke(domain: String, optional: Boolean): RegistryDelegate.NullableRegistryDelegate<T> =
        invoke(domain).optional

    override fun getByTag(tag: Identifier): Set<RegistryObject<T>>? = internalTags[tag]

    override val tags: Set<Identifier>
        get() = ImmutableSet.copyOf(internalTags.keys)

    override fun registerTags(values: Map<Identifier, Iterable<Identifier>>) {
        values.mapValuesTo(internalTags) { (tag, ids) ->
            ids.map {
                this.getPrivately(it).apply {
                    addTag(tag) // I know, side effects bad, but this means less iterating
                }
            }.toSet()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SilicaRegistry<*>) return false

        if (id != other.id) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    private class SilicaRegistryEntry<T : RegistryEntry<T>>(
        override val registry: SilicaRegistry<T>,
        override val id: Identifier
    ) : RegistryObject<T> {
        private var hasCached = false
        private var cachedValue: T? = null
        private val _tags = mutableSetOf<Identifier>()

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

        override fun hasTag(tag: Identifier) = tag in _tags

        override val tags: Set<Identifier> = ImmutableSet.copyOf(_tags)

        fun addTag(tag: Identifier) {
            _tags += tag
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SilicaRegistryEntry<*>) return false

            if (registry != other.registry) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = registry.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }
    }
}