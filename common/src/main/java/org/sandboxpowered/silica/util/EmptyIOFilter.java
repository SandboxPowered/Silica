package org.sandboxpowered.silica.util;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

public class EmptyIOFilter implements IOFileFilter {
    @Override
    public boolean accept(File file) {
        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        return false;
    }
}
