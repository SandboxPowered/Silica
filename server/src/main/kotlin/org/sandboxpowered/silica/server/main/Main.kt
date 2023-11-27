package org.sandboxpowered.silica.server.main

import joptsimple.ArgumentAcceptingOptionSpec
import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.server.DedicatedServer
import org.sandboxpowered.silica.server.DedicatedServerProperties.Companion.fromFile
import org.sandboxpowered.silica.server.ServerEula
import org.sandboxpowered.silica.util.extensions.ofType
import java.io.File
import java.nio.file.Paths

object Main {
    private val logger = getLogger()

    @JvmStatic
    fun main(args: Array<String>) {
        val spec = OptionParser()
        spec.allowsUnrecognizedOptions()
        val initSettingsSpec = spec.accepts("initSettings", "Initializes the server.properties file and then quits")
        val universeSpec = spec.accepts("universe", "The folder in which to look for world folders. (default: `.`, the current directory)")
            .withOptionalArg().ofType<File>()
        val worldSpec = spec.accepts("world", "The name of the world to load. (default: `world`)")
            .withOptionalArg().ofType<File>()
        val portSpec = spec.accepts("port", "The port to listen on. (default: `25565`)")
            .withOptionalArg().ofType<Int>()
        val unknownOptionsSpec: OptionSpec<String> = spec.nonOptions()
        val set = spec.parse(*args)
        val unknownOptions = set.valuesOf(unknownOptionsSpec)
        if (unknownOptions.isNotEmpty()) {
            logger.warn("Ignoring arguments: {}", unknownOptions)
        }
        val eula = ServerEula(Paths.get("eula.txt"))
        val universe = set.valueIfExists(universeSpec)
        val world = set.valueIfExists(worldSpec)
        val port = set.valueIfExists(portSpec)
        val dediArgs = DedicatedServer.Args(universe, world, port)
        if (set.has("initSettings")) {
            fromFile(Paths.get("server.properties"), dediArgs)
        } else {
            if (!eula.accepted) {
                LogManager.getLogger("Minecraft EULA")
                    .error("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.")
                return
            }
            DedicatedServer(dediArgs).run()
        }
    }
}

private fun <T> OptionSet.valueIfExists(spec: ArgumentAcceptingOptionSpec<T>): T? {
    return if (has(spec)) valueOf(spec) else null
}

