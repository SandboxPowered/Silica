package org.sandboxpowered.silica.api.plugin

interface PluginManager {
    val loadedPlugins: Set<String>

    fun disablePlugin(plugin: String)
}