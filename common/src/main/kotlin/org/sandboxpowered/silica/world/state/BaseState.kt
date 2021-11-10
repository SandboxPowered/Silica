package org.sandboxpowered.silica.world.state

import com.google.common.collect.ArrayTable
import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Table
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.util.extensions.set
import org.sandboxpowered.silica.world.state.property.Property

open class BaseState<B : RegistryEntry<B>, S : PropertyContainer<S>>(
    protected val base: B,
    val properties: ImmutableMap<Property<*>, Comparable<*>>
) : PropertyContainer<S> {

    private lateinit var possibleStates: Table<Property<*>, Comparable<*>, S>

    override fun <T : Comparable<T>> get(property: Property<T>): T {
        val value = properties[property]
        require(value != null) { "Cannot get property $property as it does not exist in $base" }
        return property.valueType.cast(value)
    }

    override fun <T : Comparable<T>> set(property: Property<T>, value: T): S {
        val currentValue = properties[property]
        require(currentValue != null) { "Cannot set property $property as it does not exist in $base" }
        if (currentValue == value) return this as S
        val state = getState(property, value)
        return state ?: error("Cannot set property $property to $value on $base, it is not an allowed value")
    }

    override fun <T : Comparable<T>> contains(property: Property<T>): Boolean = property in properties

    override fun <T : Comparable<T>> cycle(property: Property<T>): S {
        val currentValue = properties[property] ?: error("Cannot set property $property as it does not exist in $base")
        return set(property, findNextInCollection(property.values, currentValue as T))
    }

    fun initTable(map: Map<Map<Property<*>, Comparable<*>>, S>) {
        val table: Table<Property<*>, Comparable<*>, S> = HashBasedTable.create()
        for ((property, value) in properties) {
            val comparableIterator = property.values.iterator()
            while (comparableIterator.hasNext()) {
                val comparable = comparableIterator.next()
                if (comparable != value) {
                    map[createPropertiesCollectionWith(property, comparable)]?.let {
                        table[property, comparable] = it
                    }
                }
            }
        }
        possibleStates = if (table.isEmpty) table else ArrayTable.create(table)
    }

    private fun createPropertiesCollectionWith(property: Property<*>, comparable: Comparable<*>) =
        HashMap(properties).apply { set(property, comparable) }

    private fun <T : Comparable<T>, V : T> getState(property: Property<T>, value: V): S? =
        possibleStates[property, value] // TODO: replace with global state holder object rather than holding every possible state within all states.

    override fun toString(): String = "${javaClass.simpleName}{base=$base, properties=$properties}"

    private fun <T> findNextInCollection(collection: Collection<T>, value: T): T {
        val iterator = collection.iterator()
        do {
            if (!iterator.hasNext()) {
                return iterator.next()
            }
        } while (iterator.next() != value)
        return if (iterator.hasNext()) iterator.next() else collection.iterator().next()
    }

    fun modify(block: StateModifier<S>.() -> Unit): S {
        return StateModifier(this).apply(block).state as S
    }
}

class StateModifier<V : PropertyContainer<V>>(var state: PropertyContainer<V>) {
    infix fun <T : Comparable<T>> Property<T>.set(value: T) {
        state = state.set(this, value)
    }
}