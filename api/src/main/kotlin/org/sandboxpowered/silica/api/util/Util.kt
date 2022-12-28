package org.sandboxpowered.silica.api.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

fun Any.getLogger(): Logger = LogManager.getLogger(this::class.java)
inline fun <reified T> getLogger(): Logger = LogManager.getLogger(T::class.java)
