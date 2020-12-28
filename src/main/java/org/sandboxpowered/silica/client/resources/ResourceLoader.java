package org.sandboxpowered.silica.client.resources;

import org.sandboxpowered.silica.client.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Predicate;

public interface ResourceLoader {
    private static String getFilename(String type, Identifier identifier) {
        return String.format("%s/%s/%s", "assets", identifier.getNamespace(), identifier.getPath());
    }

    boolean containsFile(String path);

    InputStream openFile(String path) throws IOException;

    Set<String> findResources(String namespace, String path, int depth, Predicate<String> filter);

    Set<String> getNamespaces();
}