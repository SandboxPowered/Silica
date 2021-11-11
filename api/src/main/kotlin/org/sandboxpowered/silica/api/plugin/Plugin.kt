package org.sandboxpowered.silica.api.plugin

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(val id: String, val version: String, val requirements: Array<String> = [])
