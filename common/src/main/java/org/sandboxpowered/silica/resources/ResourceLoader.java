package org.sandboxpowered.silica.resources;

import org.sandboxpowered.api.util.Identity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Predicate;

public interface ResourceLoader {
    private static String getFilename(ResourceType type, Identity identity) {
        return String.format("%s/%s/%s", type.getFolder(), identity.getNamespace(), identity.getPath());
    }

    boolean containsFile(ResourceType type, String path);

    InputStream openFile(ResourceType type, String path) throws IOException;

    Set<String> findResources(ResourceType type, String namespace, String path, int depth, Predicate<String> filter);

    Set<String> getNamespaces(ResourceType type);
}