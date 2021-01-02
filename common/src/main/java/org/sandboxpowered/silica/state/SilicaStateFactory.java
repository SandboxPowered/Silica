package org.sandboxpowered.silica.state;

import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.PropertyContainer;

import java.util.Collection;
import java.util.Collections;

public class SilicaStateFactory<A, B extends PropertyContainer<B>> implements StateFactory<A, B> {
    private final A base;
    private B baseState;

    public SilicaStateFactory(A base) {
        this.base = base;
    }

    @Override
    public B getBaseState() {
        return baseState;
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