package org.sandboxpowered.silica.client.main

import com.google.inject.Guice
import com.google.inject.Injector
import joptsimple.ArgumentAcceptingOptionSpec
import joptsimple.OptionParser
import joptsimple.OptionSpec
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.client.Silica
import org.sandboxpowered.silica.client.Silica.Args
import org.sandboxpowered.silica.inject.SilicaImplementationModule
import kotlin.reflect.KClass

object Main {
    val LOG = LogManager.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val optionSpec = OptionParser()
        optionSpec.allowsUnrecognizedOptions()
        val injector: Injector = Guice.createInjector(SilicaImplementationModule())
        val widthSpec: OptionSpec<Int> =
            optionSpec.accepts("width").withRequiredArg().ofType(Int::class).defaultsTo(1000)
        val heightSpec: OptionSpec<Int> =
            optionSpec.accepts("height").withRequiredArg().ofType(Int::class).defaultsTo(563)
        val unknownOptionsSpec: OptionSpec<String> = optionSpec.nonOptions()
        val options = optionSpec.parse(*args)
        val unknownOptions = options.valuesOf(unknownOptionsSpec)
        if (unknownOptions.isNotEmpty()) {
            LOG.warn("Ignoring arguments: {}", unknownOptions)
        }
        try {
            Silica(Args(options.valueOf(widthSpec), options.valueOf(heightSpec))).run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun <V, T : Any> ArgumentAcceptingOptionSpec<V>.ofType(kClass: KClass<T>): ArgumentAcceptingOptionSpec<T> {
    return ofType(kClass.java)
}
