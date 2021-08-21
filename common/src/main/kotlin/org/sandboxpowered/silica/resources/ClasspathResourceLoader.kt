package org.sandboxpowered.silica.resources

import java.io.InputStream
import java.util.function.Predicate

class ClasspathResourceLoader : ResourceLoader {
    override fun containsFile(type: ResourceType, path: String): Boolean {
        return false
    }

    override fun openFile(type: ResourceType, path: String): InputStream {
        TODO("Not yet implemented")
    }

    override fun findResources(
        type: ResourceType,
        namespace: String,
        path: String,
        depth: Int,
        filter: Predicate<String>
    ): Set<String> {
        return emptySet()
    }

    override fun getNamespaces(type: ResourceType): Set<String> {
        return emptySet()
    }
}