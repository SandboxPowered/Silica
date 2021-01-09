package org.sandboxpowered.silica.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class ClasspathResourceLoader implements ResourceLoader {
    @Override
    public boolean containsFile(String path) {
        return false;
    }

    @Override
    public InputStream openFile(String path) throws IOException {
        return null;
    }

    @Override
    public Set<String> findResources(String namespace, String path, int depth, Predicate<String> filter) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getNamespaces() {
        return Collections.emptySet();
    }
}
