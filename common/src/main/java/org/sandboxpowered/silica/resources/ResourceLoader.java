package org.sandboxpowered.silica.resources;

import org.sandboxpowered.api.util.Identity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Predicate;

public interface ResourceLoader {
    private static String getFilename(String type, Identity identity) {
        return String.format("%s/%s/%s", "assets", identity.getNamespace(), identity.getPath());
    }

    boolean containsFile(String path);

    InputStream openFile(String path) throws IOException;

    Set<String> findResources(String namespace, String path, int depth, Predicate<String> filter);

    Set<String> getNamespaces();
}