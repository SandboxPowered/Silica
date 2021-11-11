package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.api.util.Identifier
import java.io.InputStream

class ResourceManager(private val resourceType: ResourceType) {
    private val loaders: ArrayList<ResourceLoader> = arrayListOf()

    fun add(loader: ResourceLoader) {
        loaders.add(loader)
    }

    fun contains(type: ResourceType, file: Identifier): Boolean {
        return loaders.any { it.contains(type, file) }
    }

    fun open(type: ResourceType, file: Identifier): InputStream {
        return loaders.firstOrNull { it.contains(type, file) }?.open(type, file)
            ?: error("Resource [$file] not found in ${type.folder}")
    }

    fun tryOpen(type: ResourceType, file: Identifier): InputStream? =
        if (contains(type, file)) open(type, file) else null

    fun getNamespaces(): Set<String> {
        return when (loaders.size) {
            0 -> emptySet()
            1 -> loaders.first().getNamespaces(resourceType)
            else -> loaders.flatMap { it.getNamespaces(resourceType) }.toSet()
        }
    }
}