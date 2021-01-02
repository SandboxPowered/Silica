package org.sandboxpowered.silica.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.api.state.property.PropertyContainer;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class SilicaStateFactory<A, B extends PropertyContainer<B>> implements StateFactory<A, B> {
    private final A base;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<B> states;

    public SilicaStateFactory(A base, Map<String, Property<?>> map, BiFunction<A, ImmutableMap<Property<?>, Comparable<?>>, B> stateCreator) {
        this.base = base;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);

        // Collect all possible values for set properties
        Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());
        for (Map.Entry<String, Property<?>> entry : propertiesByName.entrySet()) {
            stream = stream.flatMap(list -> entry.getValue().getValues().stream().map(comparable -> {
                List<Pair<Property<?>, Comparable<?>>> values = Lists.newArrayList(list);
                values.add(Pair.of(entry.getValue(), comparable));
                return values;
            }));
        }

        Map<Map<Property<?>, Comparable<?>>, B> propertyToState = new LinkedHashMap<>();
        List<B> allStates = new ArrayList<>();

        // Generate combined states with all possible values
        stream.forEach(list -> {
            ImmutableMap<Property<?>, Comparable<?>> propertyValues = list.stream().collect(ImmutableMap.toImmutableMap(Pair::getKey, Pair::getValue));
            B state = stateCreator.apply(base, propertyValues);
            propertyToState.put(propertyValues, state);
            allStates.add(state);
        });

        // Initialize the tables within each state allowing them to know other state values
        allStates.forEach(state -> {
            if (state instanceof BaseState) {
                ((BaseState<A, B>) state).initTable(propertyToState);
            }
        });
        this.states = ImmutableList.copyOf(allStates);
    }

    @Override
    public B getBaseState() {
        return states.get(0);
    }

    @Override
    public A getBaseObject() {
        return base;
    }

    @Override
    public Collection<B> getValidStates() {
        return Collections.emptySet();
    }
}