package org.sandboxpowered.silica.util

import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.SystemUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.sandboxpowered.silica.util.extensions.downloadFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object Util {

    const val MINECRAFT_VERSION = "1.17.1"

    private const val LAUNCHMETA_JSON = "https://launchermeta.mojang.com/mc/game/version_manifest.json"

    inline fun <reified T> getLogger(): Logger = LogManager.getLogger(T::class.java)

    private val ktorClient = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    fun ensureMinecraftVersion(version: String, side: Side): File {
        val file = File("versions/$version-${side.str}.jar")
        file.parentFile.mkdirs()
        if (!file.exists() && side == Side.CLIENT) {
            findMinecraft(version)?.copyTo(file, true)
        }
        if (!file.exists()) {
            runBlocking {
                val response: JsonObject = ktorClient.get(LAUNCHMETA_JSON)
                val versions = response.getAsJsonArray("versions")
                val v = versions.asSequence().map { it.asJsonObject }.firstOrNull {
                    it.getAsJsonPrimitive("id").asString == version
                }
                if (v != null) {
                    val res: JsonObject = ktorClient.get(v.get("url").asString)
                    val download = res.getAsJsonObject("downloads").getAsJsonObject(side.str)

                    ktorClient.downloadFile(file, download.get("url").asString)
                }
            }
        }
        return file
    }

    private fun findMinecraft(version: String): File? = when {
        SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX -> {
            val homeDir = when {
                SystemUtils.IS_OS_LINUX -> System.getenv("HOME")
                else -> System.getenv("APPDATA")
            }
            val asPath = Paths.get(homeDir, ".minecraft", "versions", version, "$version.jar")
            if (Files.exists(asPath)) asPath.toFile() else null
        }
        else -> null
    }
}