package org.sandboxpowered.silica.state;

import com.google.common.collect.ImmutableMap;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.api.state.property.PropertyContainer;

import java.util.Collection;
import java.util.Iterator;

public class BaseState<A,B> implements PropertyContainer<B> {
    protected final A base;
    private final ImmutableMap<Property<?>, Comparable<?>> properties;

    public BaseState(A base, ImmutableMap<Property<?>, Comparable<?>> properties) {
        this.base = base;
        this.properties = properties;
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
    public <T extends Comparable<T>, V extends T> B with(Property<T> property, V value) {
        Comparable<?> currentValue = this.properties.get(property);
        if (currentValue == null) {
            throw new IllegalArgumentException(String.format("Cannot set property %s as it does not exist in %s", property, this.base));
        } else {
            B state = getState(property, value);
            if (state == null) {
                throw new IllegalArgumentException(String.format("Cannot set property %s to %s on %s, it is not an allowed value", property, value, this.base));
            } else {
                return state;
            }
        }
    }

    @Override
    public <T extends Comparable<T>> B cycle(Property<T> property) {
        Comparable<?> currentValue = this.properties.get(property);
        if (currentValue == null) {
            throw new IllegalArgumentException(String.format("Cannot set property %s as it does not exist in %s", property, this.base));
        }
        return getState(property, findNextInCollection(property.getValues(), (T) currentValue));
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

    private <T extends Comparable<T>, V extends T> B getState(Property<T> property, V value) {
        return null; // TODO
    }

    @Override
    public <T extends Comparable<T>> boolean contains(Property<T> property) {
        return properties.containsKey(property);
    }
}