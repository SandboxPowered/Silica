package org.sandboxpowered.silica.util

import com.google.common.base.Objects

class Identifier private constructor(val namespace: String, val path: String) : Comparable<Identifier> {
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
        val that = other as Identifier
        return Objects.equal(namespace, that.namespace) && Objects.equal(
            path, that.path
        )
    }

    override fun hashCode(): Int {
        return Objects.hashCode(namespace, path)
    }

    companion object {
        fun of(id: String): Identifier {
            val identity = arrayOf("minecraft", id)
            val idx = id.indexOf(':')
            if (idx >= 0) {
                identity[1] = id.substring(idx + 1)
                if (idx >= 1) {
                    identity[0] = id.substring(0, idx)
                }
            }
            return of(identity[0], identity[1])
        }

        fun of(namespace: String, path: String): Identifier = Identifier(namespace, path)
    }
}