package org.sandboxpowered.silica.world.state

import org.sandboxpowered.silica.world.state.property.Property

sealed interface PropertyContainer<S : PropertyContainer<S>> {
    operator fun <T : Comparable<T>> get(property: Property<T>): T
    operator fun <T : Comparable<T>> set(property: Property<T>, value: T): S
    operator fun <T : Comparable<T>> contains(property: Property<T>): Boolean
    fun <T : Comparable<T>> cycle(property: Property<T>): S
}