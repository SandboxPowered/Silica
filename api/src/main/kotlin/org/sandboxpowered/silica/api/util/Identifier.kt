package org.sandboxpowered.silica.api.util

import com.google.common.base.Objects

class Identifier(val namespace: String, val path: String) : Comparable<Identifier> {
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
        return Objects.equal(namespace, that.namespace) && Objects.equal(path, that.path)
    }

    override fun hashCode(): Int = Objects.hashCode(namespace, path)

    fun affix(prefix: String, suffix: String) = Identifier(namespace, "$prefix$path$suffix")

    fun prefix(prefix: String) = Identifier(namespace, "$prefix$path")

    fun suffix(suffix: String) = Identifier(namespace, "$path$suffix")

    operator fun plus(suffix: String) = this.suffix(suffix)

    companion object {
        operator fun invoke(id: String): Identifier = id.split(":").let {
            when (it.size) {
                1 -> Identifier("minecraft", id)
                2 -> Identifier(it[0], it[1])
                else -> error("Couldn't parse $id")
            }
        }
    }
}