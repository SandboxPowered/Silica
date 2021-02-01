package org.sandboxpowered.silica.state

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedMap
import org.sandboxpowered.api.content.Content
import org.sandboxpowered.api.state.StateFactory
import org.sandboxpowered.api.state.property.Property
import org.sandboxpowered.api.state.property.PropertyContainer
import java.util.*
import kotlin.collections.ArrayList

class SilicaStateFactory<A: Content<A>, B : PropertyContainer<B>?>(
    private val base: A,
    map: Map<String, Property<*>>,
    stateCreator: Factory<A, B>
) : StateFactory<A, B> {
    private val propertiesByName: ImmutableSortedMap<String, Property<*>> = ImmutableSortedMap.copyOf(map)
    private val states: ImmutableList<B>

    interface Factory<A, B : PropertyContainer<B>?> {
        fun create(base: A, values: ImmutableMap<Property<*>?, Comparable<*>?>?): B
    }

    override fun getBaseState(): B {
        return states[0]
    }

    override fun getBaseObject(): A {
        return base
    }

    override fun getValidStates(): Collection<B> {
        return states
    }

    init {
        // Collect all possible values for set properties
        var sequence: Sequence<List<Pair<Property<*>, Comparable<*>>>> = sequenceOf(emptyList())
        propertiesByName.forEach { (_, property) ->
            sequence = sequence.flatMap { list ->
                property.values.map { comparable ->
                    list + (property to comparable)
                }
            }
        }

        val propertyToState: MutableMap<Map<Property<*>, Comparable<*>>, B> = LinkedHashMap()
        val allStates: MutableList<B> = ArrayList()

        // Generate combined states with all possible values
        sequence.forEach { list ->
            val propertyValues = list.stream()
                .collect(ImmutableMap.toImmutableMap(
                    { obj: Pair<Property<*>, Comparable<*>> -> obj.first },
                    { obj: Pair<Property<*>, Comparable<*>> -> obj.second }
                ))
            val state = stateCreator.create(base, propertyValues)
            propertyToState[propertyValues] = state
            allStates.add(state)
        }

        // Initialize the tables within each state allowing them to know other state values
        allStates.forEach { state ->
            if (state is BaseState<*, *>) {
                (state as BaseState<A, B>).initTable(propertyToState)
            }
        }
        states = ImmutableList.copyOf(allStates)
    }
}