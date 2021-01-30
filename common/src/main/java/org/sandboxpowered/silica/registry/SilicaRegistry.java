package org.sandboxpowered.silica.registry;

import org.jetbrains.annotations.Nullable;
import org.sandboxpowered.api.content.Content;
import org.sandboxpowered.api.registry.Registry;
import org.sandboxpowered.api.util.Identity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SilicaRegistry<T extends Content<T>> implements Registry<T> {
    private final Identity registryId;
    private final Class<T> type;

    public Map<Identity, T> internalMap = new HashMap<>();

    public Map<Identity, SilicaRegistryEntry<T>> registryEntries = new HashMap<>();

    public SilicaRegistry(Identity registryId, Class<T> type) {
        this.registryId = registryId;
        this.type = type;
    }

    public <X extends Content<X>> Registry<X> cast() {
        return (Registry<X>) this;
    }

    @Override
    public Identity getIdentity(T val) {
        return val.getIdentity();
    }

    @Override
    public Entry<T> get(Identity identity) {
        return registryEntries.computeIfAbsent(identity, id -> new SilicaRegistryEntry<>(this, id));
    }

    @Override
    public Entry<T> register(T val) {
        internalMap.put(val.getIdentity(), val);
        return get(val.getIdentity());
    }

    @Override
    public Stream<T> stream() {
        return registryEntries.values().stream().filter(Entry::isPresent).map(Entry::get);
    }

    @Override
    public Collection<Identity> keys() {
        return registryEntries.keySet();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Identity getIdentity() {
        return registryId;
    }

    public void clearCache() {
        registryEntries.forEach((id, aEntry) -> aEntry.clearCache());
    }

    public static class SilicaRegistryEntry<T extends Content<T>> implements Registry.Entry<T> {
        private final SilicaRegistry<T> registry;
        private final Identity target;
        private boolean hasCached;
        private T cachedValue;

        public SilicaRegistryEntry(SilicaRegistry<T> registry, Identity target) {
            this.registry = registry;
            this.target = target;
        }

        public void updateCache() {
            if (!hasCached) {
                cachedValue = registry.internalMap.get(target);
                hasCached = true;
            }
        }

        public void clearCache() {
            cachedValue = null;
            hasCached = false;
        }

        @Nullable
        public T getInternal() {
            updateCache();
            return cachedValue;
        }

        @Override
        public T get() {
            return getInternal();
        }

        @Override
        public Optional<T> getAsOptional() {
            return Optional.ofNullable(getInternal());
        }

        @Override
        public T orElse(T other) {
            T val = getInternal();
            return val == null ? other : val;
        }

        @Override
        public T orElseGet(Supplier<T> other) {
            T val = getInternal();
            return val == null ? other.get() : val;
        }

        @Override
        public boolean isPresent() {
            updateCache();
            return cachedValue != null;
        }

        @Override
        public boolean matches(T other) {
            T val = getInternal();
            return val == other;
        }

        @Override
        public void ifPresent(Consumer<T> tConsumer) {
            T val = getInternal();
            if (val != null)
                tConsumer.accept(val);
        }

        @Override
        public void ifPresentOrElse(Consumer<T> tConsumer, Runnable notPresent) {
            T val = getInternal();
            if (val != null)
                tConsumer.accept(val);
            else
                notPresent.run();
        }
    }
}