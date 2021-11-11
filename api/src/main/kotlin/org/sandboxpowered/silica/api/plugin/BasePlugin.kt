package org.sandboxpowered.silica.api.plugin

import org.sandboxpowered.silica.api.registry.PluginRegistrar

interface BasePlugin {
    fun onEnable() = Unit
    fun register(registrar: PluginRegistrar) = Unit
    fun onDisable() = Unit
}