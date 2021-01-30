package org.sandboxpowered.silica.resources

class ResourceManager {
    private val loaders: ArrayList<ResourceLoader> = arrayListOf()

    fun add(loader: ResourceLoader) {
        loaders.add(loader)
    }

    fun getNamespaces(): Set<String> {
        return when (loaders.size) {
            0 -> emptySet()
            1 -> loaders.first().namespaces
            else -> loaders.flatMap { it.namespaces }.toSet()
        }
    }
}