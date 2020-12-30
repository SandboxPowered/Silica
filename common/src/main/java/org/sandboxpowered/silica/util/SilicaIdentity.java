package org.sandboxpowered.silica.util;

import org.sandboxpowered.api.util.Identity;

public class SilicaIdentity implements Identity {
    public final String namespace;
    public final String path;

    public SilicaIdentity(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }
}
