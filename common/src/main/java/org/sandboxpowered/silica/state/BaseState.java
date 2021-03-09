package org.sandboxpowered.silica.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.api.content.Content;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.api.state.property.PropertyContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BaseState<B extends Content<B>, S> implements PropertyContainer<S>, Comparable<S> {
    protected final B base;
    private final ImmutableMap<Property<?>, Comparable<?>> properties;
    private Table<Property<?>, Comparable<?>, S> possibleStates;

    public BaseState(B base, ImmutableMap<Property<?>, Comparable<?>> properties) {
        this.base = base;
        this.properties = properties;
    }

    @Override
    public int compareTo(@NotNull S o) {
        if(o instanceof BaseState)
            return base.getIdentity().toString().compareTo(((BaseState<?, ?>) o).base.getIdentity().toString());
        return 1;
    }

    protected static <T> T findNextInCollection(Collection<T> collection, T object) {
        Iterator<T> iterator = collection.iterator();

        do {
            if (!iterator.hasNext()) {
                return iterator.next();
            }
        } while (!iterator.next().equals(object));

        return iterator.hasNext() ? iterator.next() : collection.iterator().next();
    }

    public ImmutableMap<Property<?>, Comparable<?>> getProperties() {
        return properties;
    }

    @Override
    public <T extends Comparable<T>> T get(Property<T> property) {
        Comparable<?> value = this.properties.get(property);
        if (value == null) {
            throw new IllegalArgumentException(String.format("Cannot get property %s as it does not exist in %s", property, this.base));
        } else {
            return property.getValueType().cast(value);
        }
    }

    @Override
    public <T extends Comparable<T>, V extends T> S with(Property<T> property, V value) {
        Comparable<?> currentValue = this.properties.get(property);
        if (currentValue == null) {
            throw new IllegalArgumentException(String.format("Cannot set property %s as it does not exist in %s", property, this.base));
        } else {
            S state = getState(property, value);
            if (state == null) {
                throw new IllegalArgumentException(String.format("Cannot set property %s to %s on %s, it is not an allowed value", property, value, this.base));
            } else {
                return state;
            }
        }
    }

    @Override
    public <T extends Comparable<T>> S cycle(Property<T> property) {
        Comparable<?> currentValue = this.properties.get(property);
        if (currentValue == null) {
            throw new IllegalArgumentException(String.format("Cannot set property %s as it does not exist in %s", property, this.base));
        }
        return getState(property, findNextInCollection(property.getValues(), (T) currentValue));
    }

    public void initTable(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.possibleStates != null) {
            throw new IllegalStateException();
        } else {
            Table<Property<?>, Comparable<?>, S> table = HashBasedTable.create();

            for (Map.Entry<Property<?>, Comparable<?>> entry : this.properties.entrySet()) {
                Property<?> property = entry.getKey();
                Iterator<Comparable<?>> comparableIterator = (Iterator<Comparable<?>>) property.getValues().iterator();

                while (comparableIterator.hasNext()) {
                    Comparable<?> comparable = comparableIterator.next();
                    if (comparable != entry.getValue()) {
                        table.put(property, comparable, map.get(this.createPropertiesCollectionWith(property, comparable)));
                    }
                }
            }

            this.possibleStates = table.isEmpty() ? table : ArrayTable.create(table);
        }
    }

    private Map<Property<?>, Comparable<?>> createPropertiesCollectionWith(Property<?> property, Comparable<?> comparable) {
        Map<Property<?>, Comparable<?>> map = new HashMap<>(this.properties);
        map.put(property, comparable);
        return map;
    }

    private <T extends Comparable<T>, V extends T> S getState(Property<T> property, V value) {
        return possibleStates.get(property, value); // TODO: replace with global state holder object rather than holding every possible state within all states.
    }

    @Override
    public <T extends Comparable<T>> boolean contains(Property<T> property) {
        return properties.containsKey(property);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "base=" + base.getIdentity() +
                ", properties=" + properties +
                '}';
    }
}