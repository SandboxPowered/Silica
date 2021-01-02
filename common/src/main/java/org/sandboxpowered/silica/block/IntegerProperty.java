package org.sandboxpowered.silica.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.sandboxpowered.api.state.property.AbstractProperty;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class IntegerProperty extends AbstractProperty<Integer> {
    private final ImmutableSet<Integer> values;

    private IntegerProperty(String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        } else if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        Set<Integer> set = Sets.newHashSet();
        for (int val = min; val <= max; ++val) {
            set.add(val);
        }
        this.values = ImmutableSet.copyOf(set);
    }

    public static IntegerProperty of(String name, int min, int max) {
        return new IntegerProperty(name, min, max);
    }

    @Override
    public String getName(Integer value) {
        return value.toString();
    }

    @Override
    public Collection<Integer> getValues() {
        return values;
    }

    @Override
    public Optional<Integer> getValue(String name) {
        Integer i = Integer.valueOf(name);
        if (values.contains(i)) {
            return Optional.of(i);
        } else {
            return Optional.empty();
        }
    }
}