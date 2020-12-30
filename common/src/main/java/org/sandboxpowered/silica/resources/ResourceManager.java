package org.sandboxpowered.silica.resources;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceManager {
    private final List<ResourceLoader> loaders = new ArrayList<>();

    public void add(ResourceLoader loader) {
        loaders.add(loader);
    }

    public Set<String> getNamespaces() {
        return switch (loaders.size()) {
            case 0 -> Collections.emptySet();
            case 1 -> loaders.get(0).getNamespaces();
            default -> loaders.stream().map(ResourceLoader::getNamespaces).flatMap(Collection::stream).collect(Collectors.toSet());
        };
    }
}
