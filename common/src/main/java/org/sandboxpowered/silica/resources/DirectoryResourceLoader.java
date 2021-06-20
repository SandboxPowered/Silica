package org.sandboxpowered.silica.resources;

import com.google.common.collect.Sets;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.api.util.Identity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class DirectoryResourceLoader implements ResourceLoader {
    private final File directory;
    private Set<String> namespaces;

    public DirectoryResourceLoader(File directory) {
        this.directory = directory;
    }

    protected static String relativize(File file, File file2) {
        return file.toURI().relativize(file2.toURI()).getPath();
    }

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
        if (namespaces == null) {
            Set<String> set = Sets.newHashSet();
            File file = new File(this.directory, "assets");
            File[] files = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            if (files != null) {
                for (File file2 : files) {
                    String string = relativize(file, file2);
                    set.add(string.substring(0, string.length() - 1));
                }
            }
            namespaces = set;
        }
        return namespaces;
    }

    @NotNull
    @Override
    public String getFilename(@NotNull ResourceType type, @NotNull Identity identity) {
        return null;
    }
}