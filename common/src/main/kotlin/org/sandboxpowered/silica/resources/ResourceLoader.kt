package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.util.Identifier
import java.io.InputStream
import java.util.function.Predicate

interface ResourceLoader {
    fun containsFile(type: ResourceType, path: String): Boolean

    fun openFile(type: ResourceType, path: String): InputStream

    fun findResources(
        type: ResourceType,
        namespace: String,
        path: String,
        depth: Int,
        filter: Predicate<String>
    ): Set<String>

    fun getNamespaces(type: ResourceType): Set<String>

    companion object {
        private fun getFilename(type: ResourceType, identifier: Identifier): String {
            return "${type.folder}/${identifier.namespace}/${identifier.path}"
        }
    }
}