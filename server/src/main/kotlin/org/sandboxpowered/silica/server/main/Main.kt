package org.sandboxpowered.silica.server.main

import joptsimple.OptionParser
import joptsimple.OptionSpec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.sandboxpowered.silica.server.DedicatedServer
import org.sandboxpowered.silica.server.ServerEula
import org.sandboxpowered.silica.server.ServerProperties.Companion.fromFile
import org.sandboxpowered.silica.util.Util.getLogger
import org.sandboxpowered.silica.util.extensions.ofType
import java.nio.file.Paths

object Main {
    private val logger = getLogger<Main>()

    @JvmStatic
    fun main(args: Array<String>) {
        val spec = OptionParser()
        spec.allowsUnrecognizedOptions()
        spec.accepts("initSettings")
        val minecraftPath = spec.accepts("minecraft")
            .withRequiredArg()
            .ofType(String::class)
            .defaultsTo("")
        val unknownOptionsSpec: OptionSpec<String> = spec.nonOptions()
        val set = spec.parse(*args)
        val unknownOptions = set.valuesOf(unknownOptionsSpec)
        if (unknownOptions.isNotEmpty()) {
            logger.warn("Ignoring arguments: {}", unknownOptions)
        }
        val path = set.valueOf(minecraftPath)
        val eula = ServerEula(Paths.get("eula.txt"))
        if (set.has("initSettings")) {
            fromFile(Paths.get("server.properties"))
        } else {
            if (!eula.agreed) {
                LogManager.getLogger("Minecraft EULA")
                    .error("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.")
                return
            }
            DedicatedServer(DedicatedServer.Args(
                if (path.isEmpty()) null else Paths.get(path)
            )).run()
        }
    }
}