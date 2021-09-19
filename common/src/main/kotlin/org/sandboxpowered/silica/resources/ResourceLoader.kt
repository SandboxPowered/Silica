package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.util.Identifier
import java.io.InputStream
import java.util.function.Predicate

interface ResourceLoader {
    val name: String

    fun contains(type: ResourceType, file: Identifier): Boolean

    fun open(file: String): InputStream?

    fun open(type: ResourceType, file: Identifier): InputStream

    fun findResources(
        type: ResourceType,
        namespace: String,
        path: String,
        depth: Int,
        filter: Predicate<String>
    ): Set<Identifier>

    fun getNamespaces(type: ResourceType): Set<String>

    companion object {
        fun getPath(type: ResourceType, identifier: Identifier): String {
            return "/${type.folder}/${identifier.namespace}/${identifier.path}"
        }
    }
}