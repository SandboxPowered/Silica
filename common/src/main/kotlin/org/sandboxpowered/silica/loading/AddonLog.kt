package org.sandboxpowered.silica.loading

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.sandboxpowered.api.addon.Log
import org.sandboxpowered.internal.AddonSpec

class AddonLog(spec: AddonSpec) : Log {
    private val logger: Logger = LogManager.getLogger(spec.title)

    override fun info(message: String) = logger.info(message)

    override fun error(message: String) = logger.error(message)

    override fun debug(message: String) = logger.debug(message)

    override fun info(message: String, vararg args: Any) = logger.info(message, *args)

    override fun error(message: String, vararg args: Any) = logger.error(message, *args)

    override fun debug(message: String, vararg args: Any) = logger.debug(message, *args)

    override fun error(message: String, e: Throwable) = logger.error(message, e)
}