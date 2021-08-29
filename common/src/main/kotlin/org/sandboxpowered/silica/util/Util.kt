package org.sandboxpowered.silica.util

import com.google.common.util.concurrent.MoreExecutors
import org.apache.commons.lang3.SystemUtils
import org.joml.Math
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Util {

    const val MINECRAFT_VERSION = "1.17.1"

    fun createService(string: String): ExecutorService {
        val i = Math.clamp(1, 32, Runtime.getRuntime().availableProcessors())
        return if (i == 1) {
            MoreExecutors.newDirectExecutorService()
        } else {
            Executors.newFixedThreadPool(i)
        }
    }

    fun findMinecraft(manager: ResourceManager, minecraftPath: Path?) {
        when {
            minecraftPath != null -> {
                require(Files.exists(minecraftPath)) { "The specified path of $minecraftPath does not exist, please make sure this is targeting a Minecraft $MINECRAFT_VERSION jar file" }
                manager.add(ZIPResourceLoader(minecraftPath.toFile()))
            }
            SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX -> {
                val homeDir = when {
                    SystemUtils.IS_OS_LINUX -> System.getenv("HOME")
                    else -> System.getenv("APPDATA")
                }
                val asPath = Paths.get(homeDir, ".minecraft", "versions", MINECRAFT_VERSION, "$MINECRAFT_VERSION.jar")
                require(Files.exists(asPath)) { "Unable to find Minecraft $MINECRAFT_VERSION installation at $asPath, please install minecraft or use the --minecraft argument to point to a specific jar" }
                manager.add(ZIPResourceLoader(asPath.toFile()))
            }
            else -> {
                error("Silica supports automatically searching for Minecraft on Windows and Linux only. use the --minecraft argument on other operating systems")
            }
        }
    }
}