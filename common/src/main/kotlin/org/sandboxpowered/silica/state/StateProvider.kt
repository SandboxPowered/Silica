package org.sandboxpowered.silica.state

import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.state.property.Property

interface StateProvider<T : RegistryEntry<T>, S : PropertyContainer<S>> {
    val validStates: Collection<S>
    val baseObject: T
    val baseState: S

    interface Builder<T : RegistryEntry<T>, S : PropertyContainer<S>> {
        fun add(vararg properties: Property<*>): Builder<T, S>
    }

    interface StateFactory {
        fun <T : RegistryEntry<T>, S : PropertyContainer<S>> createBuilder(value: T): Builder<T, S>
        fun <T : RegistryEntry<T>, S : PropertyContainer<S>> create(builder: Builder<T, S>): StateProvider<T, S>
    }
}