package org.sandboxpowered.silica.api.plugin

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(
    /**
     * The id of the plugin.
     */
    val id: String,
    /**
     * The version of the plugin.
     */
    val version: String,
    /**
     * List of plugin dependencies.
     */
    val requirements: Array<String> = [],
    /**
     * List of plugins this plugin should be loaded before.
     */
    val before: Array<String> = [],
    /**
     * List of plugins this plugin should be loaded after.
     */
    val after: Array<String> = [],
    /**
     * Whether the plugin is a core compatability module
     */
    val native: Boolean = false
)
