package org.sandboxpowered.silica.server

import org.sandboxpowered.silica.api.server.ServerProperties
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

//TODO: Move to TOML or YAML based configuration
class DedicatedServerProperties(args: DedicatedServer.Args, private val properties: Properties) : ServerProperties {
    override val onlineMode = get("online-mode", true) // TODO: Only allow this in dev envs
    override val motd = get("motd", "<grey>A <red>**Sandbox Silica</red> Server</grey>")
    override val serverPort = args.port ?: get("server-port", 25565)
    override val serverIp = get("server-ip", "")
    override val maxTickTime = get("max-tick-time", 60000)
    override val maxPlayers = get("max-players", 20)
    override val supportChatFormatting: Boolean = get("support-chat-formatting", true)
    override val velocityEnabled: Boolean = get("velocity-enabled", false)
    override val velocityKey: String = get("velocity-secret", "[KEY HERE]")

    private fun getRawString(string: String): String? = properties[string] as String?

    private fun <V : Any> get(key: String, transform: (String) -> V, default: V): V =
        get(key, transform, Any::toString, default)

    private fun get(key: String, default: String): String = get(key, { it }, { it }, default)

    private fun get(key: String, default: Boolean): Boolean =
        get(key, { s: String -> java.lang.Boolean.valueOf(s) }, default)

    private fun get(key: String, default: Int): Int =
        get(key, { s: String -> Integer.valueOf(s) }, default)

    private fun <V> get(
        name: String,
        transformFromString: (String) -> V,
        transformToString: (V) -> String,
        default: V
    ): V = (getRawString(name)
        ?.let(transformFromString)
        ?: default)
        .also { properties[name] = transformToString(it) }

    private fun save(path: Path) {
        val outputStream = Files.newOutputStream(path)

        properties.store(outputStream, "Silica server properties")

        outputStream.close()
    }

    companion object {
        @JvmStatic
        fun fromFile(path: Path, args: DedicatedServer.Args): DedicatedServerProperties {
            val properties = Properties()

            if (Files.notExists(path)) Files.createFile(path)

            Files.newInputStream(path).use<InputStream, Unit>(properties::load)
            return DedicatedServerProperties(args, properties).apply { save(path) }
        }
    }
}