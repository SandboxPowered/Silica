package org.sandboxpowered.silica.client.main

import joptsimple.ArgumentAcceptingOptionSpec
import joptsimple.OptionParser
import joptsimple.OptionSpec
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.client.Silica
import org.sandboxpowered.silica.client.Silica.Args
import java.nio.file.Paths
import kotlin.reflect.KClass

object Main {
    val LOG = LogManager.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val optionSpec = OptionParser()
        optionSpec.allowsUnrecognizedOptions()
//        Guice.createInjector(SilicaImplementationModule())
        val widthSpec = optionSpec.accepts("width")
            .withRequiredArg()
            .ofType(Int::class)
            .defaultsTo(1000)
        val heightSpec = optionSpec.accepts("height")
            .withRequiredArg()
            .ofType(Int::class)
            .defaultsTo(563)
        val rendererSpec = optionSpec.accepts("renderer")
            .withRequiredArg()
            .ofType(String::class)
            .defaultsTo("")
        val minecraftPath = optionSpec.accepts("minecraft")
            .withRequiredArg()
            .ofType(String::class)
            .defaultsTo("")
        val unknownOptionsSpec: OptionSpec<String> = optionSpec.nonOptions()
        val options = optionSpec.parse(*args)
        val unknownOptions = options.valuesOf(unknownOptionsSpec)
        if (unknownOptions.isNotEmpty()) {
            LOG.warn("Ignoring arguments: {}", unknownOptions)
        }
        val path = options.valueOf(minecraftPath)
        try {
            Silica(
                Args(
                    options.valueOf(widthSpec),
                    options.valueOf(heightSpec),
                    options.valueOf(rendererSpec),
                    if (path.isEmpty()) null else Paths.get(path)
                )
            ).run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun <V, T : Any> ArgumentAcceptingOptionSpec<V>.ofType(kClass: KClass<T>): ArgumentAcceptingOptionSpec<T> {
    return ofType(kClass.java)
}
