package org.sandboxpowered.silica.resources;

import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.api.util.Identity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class ClasspathResourceLoader implements ResourceLoader {
    @Override
    public boolean containsFile(ResourceType type, String path) {
        return false;
    }

    @Override
    public InputStream openFile(ResourceType type, String path) throws IOException {
        return null;
    }

    @Override
    public Set<String> findResources(ResourceType type, String namespace, String path, int depth, Predicate<String> filter) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public String getFilename(@NotNull ResourceType type, @NotNull Identity identity) {
        return null;
    }
}
