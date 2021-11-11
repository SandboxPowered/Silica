package org.sandboxpowered.silica.api.plugin

interface BasePlugin {
    fun onEnable() = Unit
    fun onDisable() = Unit
}