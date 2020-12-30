package org.sandboxpowered.silica.resources;

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
    public static Pattern pattern = Pattern.compile("assets\\/([a-z]*)\\/");
    private final File file;
    private final ZipFile zip;
    private Set<String> namespaces;

    public ZIPResourceLoader(File file) throws IOException {
        this.file = file;
        this.zip = new ZipFile(file);
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
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            HashSet<String> namespaces = new HashSet<>();

            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                String string = zipEntry.getName();
                if (string.startsWith("assets/")) {
                    Matcher matcher = pattern.matcher(string);
                    if (matcher.find()) {
                        namespaces.add(matcher.group(1));
                    }
                }
            }

            this.namespaces = namespaces;
        }
        return this.namespaces;
    }
}