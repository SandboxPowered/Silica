package org.sandboxpowered.silica.plugin

import org.reflections.Reflections
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.util.extensions.getAnnotation
import org.sandboxpowered.silica.util.extensions.getTypesAnnotatedWith

class PluginManager {
    val plugins: MutableMap<String, Pair<Plugin, BasePlugin>> = HashMap()

    fun load() {
        val classes = Reflections("org.sandboxpowered").getTypesAnnotatedWith<Plugin>()
        val log = getLogger()
        log.info("Loading ${classes.size} plugins")
        val map = sortedMapOf<Plugin, BasePlugin>(compareBy<Plugin> { !it.native }.thenComparing { o1, o2 ->
            if (o1.id in o2.before) 1 else if (o2.id in o1.before) -1 else 0
        }.thenComparing { o1, o2 ->
            if (o1.id in o2.after) -1 else if (o2.id in o1.after) 1 else 0
        })
        //TODO: Make dependencies load in the correct order
        classes.forEach {
            val plugin = it.getAnnotation<Plugin>()
            if (BasePlugin::class.java.isAssignableFrom(it)) {
                val instance = (it.kotlin.objectInstance ?: it.getConstructor().newInstance()) as BasePlugin
                map[plugin] = instance
            }
        }
        map.forEach { (plugin, instance) ->
            plugins[plugin.id] = plugin to instance
            log.debug("Loading ${plugin.id}@${plugin.version}")
            instance.onEnable()
        }
    }
}