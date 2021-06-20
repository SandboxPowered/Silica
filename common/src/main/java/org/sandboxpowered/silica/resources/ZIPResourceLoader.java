package org.sandboxpowered.silica.resources;

import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.api.util.Identity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZIPResourceLoader implements ResourceLoader {
    public static Pattern pattern = Pattern.compile("(assets|data)\\/([a-z]*)\\/");
    private final File file;
    private final ZipFile zip;
    private Set<String> namespaces;

    public ZIPResourceLoader(File file) throws IOException {
        this.file = file;
        this.zip = new ZipFile(file);
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
        return null;
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        if (namespaces == null) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            HashSet<String> namespaces = new HashSet<>();

            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                String string = zipEntry.getName();
                if (string.startsWith(type.getFolder() + '/')) {
                    Matcher matcher = pattern.matcher(string);
                    if (matcher.find()) {
                        namespaces.add(matcher.group(2));
                    }
                }
            }

            this.namespaces = namespaces;
        }
        return this.namespaces;
    }

    @NotNull
    @Override
    public String getFilename(@NotNull ResourceType type, @NotNull Identity identity) {
        return null;
    }
}