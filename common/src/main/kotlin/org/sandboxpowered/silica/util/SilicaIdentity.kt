package org.sandboxpowered.silica.util

import com.google.common.base.Objects
import org.sandboxpowered.api.util.Identifier

class SilicaIdentity(private val namespace: String, private val path: String) : Identifier {
    override fun getNamespace(): String {
        return namespace
    }

    override fun getPath(): String {
        return path
    }

    override fun toString(): String {
        return "$namespace:$path"
    }

    override fun compareTo(other: Identifier): Int {
        return when (val ns = namespace.compareTo(other.namespace)) {
            0 -> path.compareTo(other.path)
            else -> ns
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SilicaIdentity
        return Objects.equal(namespace, that.namespace) && Objects.equal(
            path, that.path
        )
    }

    override fun hashCode(): Int {
        return Objects.hashCode(namespace, path)
    }
}