package org.sandboxpowered.silica.client.util;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

public enum FileFilters implements IOFileFilter {
    ZIP("zip"),
    JAR("jar");

    private final String extension;

    FileFilters(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(File file) {
        return accept(file.getParentFile(), file.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith("." + extension);
    }

    public IOFileFilter or(IOFileFilter other) {
        return new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return FileFilters.this.accept(file) || other.accept(file);
            }

            @Override
            public boolean accept(File dir, String name) {
                return FileFilters.this.accept(dir, name) || other.accept(dir, name);
            }
        };
    }
}
