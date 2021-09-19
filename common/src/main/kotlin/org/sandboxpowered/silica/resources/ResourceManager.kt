package org.sandboxpowered.silica.resources

class ResourceManager(private val resourceType: ResourceType) {
    private val loaders: ArrayList<ResourceLoader> = arrayListOf()

    private lateinit var _vanilla: ResourceLoader

    var vanilla: ResourceLoader
        get() = _vanilla
        set(value) {
            add(value)
            _vanilla = value
        }

    fun add(loader: ResourceLoader) {
        loaders.add(loader)
    }

    fun getVanillaLoader(): ResourceLoader {
        return vanilla
    }

    fun getNamespaces(): Set<String> {
        return when (loaders.size) {
            0 -> emptySet()
            1 -> loaders.first().getNamespaces(resourceType)
            else -> loaders.flatMap { it.getNamespaces(resourceType) }.toSet()
        }
    }
}