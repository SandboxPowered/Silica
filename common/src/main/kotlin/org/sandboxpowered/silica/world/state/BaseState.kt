package org.sandboxpowered.silica.world.state

import com.google.common.collect.ArrayTable
import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Table
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.world.state.property.Property

open class BaseState<B : RegistryEntry<B>, S : PropertyContainer<S>>(
    protected val base: B,
    val properties: ImmutableMap<Property<*>, Comparable<*>>
) : PropertyContainer<S> {

    private lateinit var possibleStates: Table<Property<*>, Comparable<*>, S>

    override fun <T : Comparable<T>> get(property: Property<T>): T {
        val value = properties[property]
        return if (value == null) {
            throw IllegalArgumentException(
                String.format(
                    "Cannot get property %s as it does not exist in %s",
                    property,
                    base
                )
            )
        } else {
            property.valueType.cast(value)
        }
    }

    override fun <T : Comparable<T>> set(property: Property<T>, value: T): S {
        val currentValue = properties[property]
        return if (currentValue == null) {
            throw error { "Cannot set property $property as it does not exist in $base" }
        } else {
            val state = getState(property, value)
            state ?: error { "Cannot set property $property to $value on $base, it is not an allowed value" }
        }
    }

    override fun <T : Comparable<T>> contains(property: Property<T>): Boolean {
        return properties.containsKey(property)
    }

    override fun <T : Comparable<T>> cycle(property: Property<T>): S {
        val currentValue = properties[property]
            ?: error { "Cannot set property $property as it does not exist in $base" }
        return set(property, findNextInCollection(property.values, currentValue as T))
    }

    fun initTable(map: Map<Map<Property<*>, Comparable<*>>, S>) {
        val table: Table<Property<*>, Comparable<*>, S> = HashBasedTable.create()
        for ((property, value) in properties) {
            val comparableIterator = property.values.iterator()
            while (comparableIterator.hasNext()) {
                val comparable = comparableIterator.next()
                if (comparable !== value) {
                    map[createPropertiesCollectionWith(property, comparable)]?.let {
                        table.put(
                            property,
                            comparable,
                            it
                        )
                    }
                }
            }
        }
        possibleStates = if (table.isEmpty) table else ArrayTable.create(table)
    }

    private fun createPropertiesCollectionWith(
        property: Property<*>,
        comparable: Comparable<*>
    ): Map<Property<*>, Comparable<*>> {
        val map: MutableMap<Property<*>, Comparable<*>> = HashMap(
            properties
        )
        map[property] = comparable
        return map
    }

    private fun <T : Comparable<T>, V : T> getState(property: Property<T>, value: V): S? {
        return possibleStates[property, value] // TODO: replace with global state holder object rather than holding every possible state within all states.
    }

    override fun toString(): String {
        return "${javaClass.simpleName}{base=$base, properties=$properties}"
    }

    private fun <T> findNextInCollection(collection: Collection<T>, value: T): T {
        val iterator = collection.iterator()
        do {
            if (!iterator.hasNext()) {
                return iterator.next()
            }
        } while (iterator.next() != value)
        return if (iterator.hasNext()) iterator.next() else collection.iterator().next()
    }
}