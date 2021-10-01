package org.sandboxpowered.silica.util

import com.google.common.base.Objects

class Identifier private constructor(val namespace: String, val path: String) : Comparable<Identifier> {
    init {
        require(namespace.isNotEmpty()) { "Identifier can not have an empty namespace. Path: $path" }
        require(path.isNotEmpty()) { "Identifier can not have an empty path. Namespace: $namespace" }
    }

    override fun toString(): String = "$namespace:$path"

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

    override fun hashCode(): Int = Objects.hashCode(namespace, path)

    companion object {
        operator fun invoke(id: String): Identifier {
            val identity = id.split(":")
            return when (identity.size) {
                1 -> invoke("minecraft", id)
                2 -> invoke(identity[0], identity[1])
                else -> error("Couldn't parse $id")
            }
        }

        operator fun invoke(namespace: String, path: String): Identifier = Identifier(namespace, path)
    }
}