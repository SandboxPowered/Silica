package org.sandboxpowered.silica.world.state

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedMap
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.world.state.property.Property

class SilicaStateFactory<B : RegistryEntry<B>, S : PropertyContainer<S>>(
    override val baseObject: B,
    map: Map<String, Property<*>>,
    stateCreator: (base: B, properties: ImmutableMap<Property<*>, Comparable<*>>) -> S
) : StateProvider<B, S> {
    private val propertiesByName: ImmutableSortedMap<String, Property<*>> = ImmutableSortedMap.copyOf(map)
    override val validStates: ImmutableList<S>
    override val baseState: S
        get() = this.validStates[0]

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
            val state = stateCreator(baseObject, propertyValues)
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