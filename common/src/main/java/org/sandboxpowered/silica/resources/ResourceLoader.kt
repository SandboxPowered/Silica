package org.sandboxpowered.silica.resources

import org.sandboxpowered.api.util.Identifier
import java.io.IOException
import java.io.InputStream
import java.util.function.Predicate

interface ResourceLoader {
    fun containsFile(type: ResourceType, path: String): Boolean

    @Throws(IOException::class)
    fun openFile(type: ResourceType, path: String): InputStream
    fun findResources(
        type: ResourceType?,
        namespace: String?,
        path: String?,
        depth: Int,
        filter: Predicate<String>
    ): Set<String>

    fun getNamespaces(type: ResourceType): Set<String>

    fun getFilename(type: ResourceType, identity: Identifier) = "${type.folder}/${identity.namespace}/${identity.path}"
}