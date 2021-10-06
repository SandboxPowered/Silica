package org.sandboxpowered.silica.server

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class DedicatedServerProperties(private val properties: Properties) : ServerProperties {
    override val onlineMode = get("online-mode", true)
    override val motd = get("motd", "<grey>A <red>**Sandbox Silica</red> Server</grey>")
    override val serverPort = get("server-port", 25565)
    override val serverIp = get("server-ip", "")
    override val maxTickTime = get("max-tick-time", 60000)
    override val maxPlayers = get("max-players", 20)

    private fun getRawString(string: String): String? = properties[string] as String?

    private fun <V> get(key: String, stringToValue: (String) -> V, defaultValue: V): V =
        get(key, stringToValue, { o: V -> Objects.toString(o) }, defaultValue)

    private fun get(key: String, defaultValue: String): String = get(key, { it }, { it }, defaultValue)

    private fun get(key: String, defaultValue: Boolean): Boolean =
        get(key, { s: String -> java.lang.Boolean.valueOf(s) }, defaultValue)

    private fun get(key: String, defaultValue: Int): Int =
        get(key, { s: String -> Integer.valueOf(s) }, defaultValue)

    private fun <V> get(
        name: String,
        stringToValue: (String) -> V,
        valueToString: (V) -> String,
        defaultValue: V
    ): V = (getRawString(name)
        ?.let(stringToValue)
        ?: defaultValue)
        .also { properties[name] = valueToString(it) }

    private fun save(path: Path) {
        val outputStream = Files.newOutputStream(path)

        properties.store(outputStream, "Silica server properties")

        outputStream.close()
    }

    companion object {
        @JvmStatic
        fun fromFile(path: Path): DedicatedServerProperties {
            val properties = Properties()

            if (Files.notExists(path)) Files.createFile(path)

            Files.newInputStream(path).use<InputStream, Unit>(properties::load)
            return DedicatedServerProperties(properties).apply { save(path) }
        }
    }
}