package org.sandboxpowered.silica.resources

import org.sandboxpowered.utilities.Identifier
import java.io.InputStream
import java.util.function.Predicate

class ResourceManager(private val resourceType: ResourceType) {
    private val loaders: ArrayList<ResourceLoader> = arrayListOf()

    fun add(loader: ResourceLoader) {
        loaders.add(loader)
    }

    fun contains(file: Identifier): Boolean {
        return loaders.any { it.contains(resourceType, file) }
    }

    fun open(file: Identifier): InputStream {
        return loaders.firstOrNull { it.contains(resourceType, file) }?.open(resourceType, file)
            ?: error("Resource [$file] not found in ${resourceType.folder}")
    }

    fun tryOpen(file: Identifier): InputStream? =
        if (contains(file)) open(file) else null

    fun listFiles(
        namespace: String? = null,
        category: String? = null,
        depth: Int = ResourceLoader.DEPTH_NO_LIMIT,
        filter: Predicate<String> = Predicate { true }
    ) = optimiseFromLoaderSize { it.findResources(resourceType, namespace, category, depth, filter) }

    fun getNamespaces(): Set<String> = optimiseFromLoaderSize { it.getNamespaces(resourceType) }

    private inline fun <T> optimiseFromLoaderSize(body: (ResourceLoader) -> Set<T>) = when (loaders.size) {
        0 -> emptySet()
        1 -> body(loaders.first())
        else -> loaders.flatMap { body(it) }.toSet()
    }
}