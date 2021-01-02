package org.sandboxpowered.silica.state;

import com.google.common.collect.Maps;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.api.state.property.PropertyContainer;

import java.util.Map;

public class SilicaStateBuilder<B, S extends PropertyContainer<S>> implements StateFactory.Builder<B, S> {
    private final B base;
    private final Map<String, Property<?>> properties = Maps.newHashMap();

    public SilicaStateBuilder(B base) {
        this.base = base;
    }

    public Map<String, Property<?>> getProperties() {
        return properties;
    }

    @Override
    public StateFactory.Builder<B, S> add(Property<?>... properties) {
        for (Property<?> property : properties) {
            this.properties.put(property.getName(), property);
        }

        return this;
    }
}
