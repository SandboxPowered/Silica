package org.sandboxpowered.silica.registry;

import org.sandboxpowered.silica.util.Identifier;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public interface RegistryObject<T extends RegistryEntry<T>> extends Supplier<T> {
    Optional<T> asOptional();

    T get() throws NoSuchElementException;

    boolean isPresent();

    boolean isEmpty();

    RegistryObject<T> or(RegistryObject<T> supplier);

    Identifier getId();

    T orElseGet(Supplier<T> supplier);

    <X extends Throwable> T orElseThrow(Supplier<X> supplier) throws X;

    Registry<T> getRegistry();
}