package org.sandboxpowered.silica.api.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("unused") // used as receiver to not specify generic arg explicitly
inline fun <reified T> T.getLogger(): Logger = LogManager.getLogger(T::class.java)
inline fun <reified T> getLogger(): Logger = LogManager.getLogger(T::class.java)
