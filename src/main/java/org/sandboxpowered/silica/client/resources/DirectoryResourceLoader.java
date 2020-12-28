package org.sandboxpowered.silica.client.resources;

import com.google.common.collect.Sets;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
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
    public boolean containsFile(String path) {
        return false;
    }

    @Override
    public InputStream openFile(String path) throws IOException {
        return null;
    }

    @Override
    public Set<String> findResources(String namespace, String path, int depth, Predicate<String> filter) {
        return null;
    }

    @Override
    public Set<String> getNamespaces() {
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
}