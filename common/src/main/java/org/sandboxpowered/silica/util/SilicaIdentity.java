package org.sandboxpowered.silica.util;

import com.google.common.base.Objects;
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

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SilicaIdentity that = (SilicaIdentity) o;
        return Objects.equal(namespace, that.namespace) && Objects.equal(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace, path);
    }
}
