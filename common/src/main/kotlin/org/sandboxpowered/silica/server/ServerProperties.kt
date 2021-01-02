package org.sandboxpowered.silica.server

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ServerProperties(private val properties: Properties) {
    val onlineMode = get("online-mode", true)
    val motd = get("motd", "A Sandbox Silica Server")
    val serverPort = get("server-port", 25565)
    val serverIp = get("server-ip", "")

    private fun getStringRaw(string: String): String? {
        return properties[string] as String?
    }

    private operator fun <V> get(name: String, stringToValue: (String) -> V, defaultValue: V): V {
        return get(name, stringToValue, { o: V -> Objects.toString(o) }, defaultValue)
    }

    private operator fun get(name: String, defaultValue: String): String {
        return get(name, { it }, { it }, defaultValue)
    }

    private operator fun get(string: String, bl: Boolean): Boolean {
        return get(string, { s: String -> java.lang.Boolean.valueOf(s) }, bl)
    }

    private operator fun get(string: String, i: Int): Int {
        return get(string, { s: String -> Integer.valueOf(s) }, i)
    }

    private operator fun <V> get(name: String, stringToValue: (String) -> V, valueToString: (V) -> String, defaultValue: V): V {
        return (getStringRaw(name)
                ?.let(stringToValue)
                ?: defaultValue)
                .also { properties[name] = valueToString(it) }
    }

    private fun save(path: Path) {
        val outputStream = Files.newOutputStream(path)

        properties.store(outputStream, "Silica server properties")

        outputStream.close()
    }

    companion object {
        @JvmStatic
        fun fromFile(path: Path): ServerProperties {
            val properties = Properties()

            if (Files.notExists(path))
                Files.createFile(path)

            val inputStream = Files.newInputStream(path)

            properties.load(inputStream)

            inputStream.close()

            val props = ServerProperties(properties)

            props.save(path)

            return props
        }
    }
}