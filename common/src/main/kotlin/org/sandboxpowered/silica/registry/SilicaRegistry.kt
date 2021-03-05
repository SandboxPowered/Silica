package org.sandboxpowered.silica.registry

import org.sandboxpowered.api.content.Content
import org.sandboxpowered.api.registry.Registry
import org.sandboxpowered.api.util.Identity
import org.sandboxpowered.silica.util.ifPresent
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Stream

class SilicaRegistry<T : Content<T>>(private val registryId: Identity, private val type: Class<T>) : Registry<T> {
    var internalMap: MutableMap<Identity, T> = HashMap()
    var registryEntries: MutableMap<Identity, SilicaRegistryEntry<T>> = HashMap()

    fun <X : Content<X>?> cast(): Registry<X> {
        return this as Registry<X>
    }

    override fun getIdentity(content: T): Identity {
        return content.identity
    }

    override fun get(identity: Identity): Registry.Entry<T> {
        return registryEntries.computeIfAbsent(identity) { id: Identity -> SilicaRegistryEntry(this, id) }
    }

    override fun register(content: T): Registry.Entry<T> {
        internalMap[content.identity] = content
        return get(content.identity)
    }

    override fun stream(): Stream<T> {
        return registryEntries.values.stream().filter { obj: SilicaRegistryEntry<T> -> obj.isPresent }
            .map { obj: SilicaRegistryEntry<T> -> obj.get() }
    }

    override fun keys(): Collection<Identity> {
        return registryEntries.keys
    }

    override fun getType(): Class<T> {
        return type
    }

    override fun getIdentity(): Identity {
        return registryId
    }

    fun clearCache() {
        registryEntries.forEach { (_: Identity, aEntry: SilicaRegistryEntry<T>) -> aEntry.clearCache() }
    }

    class SilicaRegistryEntry<T : Content<T>>(private val registry: SilicaRegistry<T>, private val target: Identity) :
        Registry.Entry<T> {
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

        override fun getAsOptional(): Optional<T> {
            return Optional.ofNullable(internal)
        }

        override fun orElse(other: T): T {
            return internal ?: other
        }

        override fun orNull(): T? {
            return internal
        }

        override fun orElseGet(other: Supplier<T>): T {
            return internal ?: other.get()
        }

        override fun isPresent(): Boolean {
            updateCache()
            return cachedValue != null
        }

        override fun matches(other: T): Boolean {
            return internal === other
        }

        override fun ifPresent(tConsumer: Consumer<T>) {
            internal.ifPresent(tConsumer::accept)
        }

        override fun ifPresentOrElse(tConsumer: Consumer<T?>, notPresent: Runnable) {
            val internalVal = internal
            if (internalVal != null) tConsumer.accept(internalVal) else notPresent.run()
        }
    }
}