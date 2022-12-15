package org.sandboxpowered.silica.util

import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.gson.GsonConverter
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.SystemUtils
import org.sandboxpowered.silica.api.util.Side
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.util.extensions.downloadFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.createTempFile

object VanillaLocator {

    private val logger = getLogger()

    const val MINECRAFT_VERSION = "1.18.2"

    private const val LAUNCHMETA_JSON = "https://launchermeta.mojang.com/mc/game/version_manifest.json"

    private val ktorClient = HttpClient {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter())
        }
    }

    fun ensureMinecraftVersion(version: String, side: Side): File {
        val loggerReference = "MC $version $side"
        val file = File("versions/$version-${side.str}.jar")
        file.parentFile.mkdirs()
        if (!file.exists()) {
            if (side == Side.CLIENT) findMinecraft(version)?.copyTo(file, true)
            else runBlocking {
                logger.info("Downloading $loggerReference...")
                val response: JsonObject = ktorClient.get(LAUNCHMETA_JSON).body()
                val versions = response.getAsJsonArray("versions")
                val v = versions.asSequence().map { it.asJsonObject }.firstOrNull {
                    it.getAsJsonPrimitive("id").asString == version
                }
                if (v != null) {
                    val res: JsonObject = ktorClient.get(v.get("url").asString).body()
                    val download = res.getAsJsonObject("downloads").getAsJsonObject(side.str)

                    val downloadFile = createTempFile("mc-${side.str}-$version", ".jar").toFile()
                    if (ktorClient.downloadFile(downloadFile, download.get("url").asString)) downloadFile else null
                } else null
            }?.also {
                val zip = ZipFile(it)
                if (
                    zip.getEntry("META-INF/versions/$version/${side.str}-$version.jar")
                        ?.let(zip::getInputStream)
                        ?.use { input -> file.outputStream().use { output -> input.copyTo(output) > 0 } } == true
                ) logger.info("Downloaded and extracted $loggerReference")
                else logger.warn("Could not extract $loggerReference")
            } ?: run { logger.warn("Could not download $loggerReference") }
        }
        return file
    }

    private fun findMinecraft(version: String): File? = when {
        SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX -> {
            val homeDir = System.getenv(if (SystemUtils.IS_OS_LINUX) "HOME" else "APPDATA")
            val asPath = Paths.get(homeDir, ".minecraft", "versions", version, "$version.jar")
            if (Files.exists(asPath)) asPath.toFile() else null
        }
        else -> null
    }
}