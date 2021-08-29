package org.sandboxpowered.silica.state

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedMap
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.state.property.Property

class SilicaStateFactory<B : RegistryEntry<B>, S : PropertyContainer<S>>(
    override val baseObject: B,
    map: Map<String, Property<*>>,
    stateCreator: Factory<B, S>
) : StateProvider<B, S> {
    private val propertiesByName: ImmutableSortedMap<String, Property<*>> = ImmutableSortedMap.copyOf(map)
    override val validStates: ImmutableList<S>
    override val baseState: S
        get() = this.validStates[0]

    interface Factory<B, S : PropertyContainer<S>> {
        fun create(base: B, values: ImmutableMap<Property<*>, Comparable<*>>): S

        companion object {
            fun <B, S : PropertyContainer<S>> of(factory: (base: B, properties: ImmutableMap<Property<*>, Comparable<*>>) -> S): Factory<B, S> {
                return object : Factory<B, S> {
                    override fun create(base: B, values: ImmutableMap<Property<*>, Comparable<*>>): S {
                        return factory.invoke(base, values)
                    }
                }
            }
        }
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

        val propertyToState: MutableMap<Map<Property<*>, Comparable<*>>, S> = LinkedHashMap()
        val allStates: MutableList<S> = ArrayList()

        // Generate combined states with all possible values
        sequence.forEach { list ->
            val propertyValues = list.stream()
                .collect(ImmutableMap.toImmutableMap(
                    { obj: Pair<Property<*>, Comparable<*>> -> obj.first },
                    { obj: Pair<Property<*>, Comparable<*>> -> obj.second }
                ))
            val state = stateCreator.create(baseObject, propertyValues)
            propertyToState[propertyValues] = state
            allStates.add(state)
        }

        // Initialize the tables within each state allowing them to know other state values
        allStates.forEach { state ->
            if (state is BaseState<*, *>) {
                (state as BaseState<B, S>).initTable(propertyToState)
            }
        }
        this.validStates = ImmutableList.copyOf(allStates)
    }
}