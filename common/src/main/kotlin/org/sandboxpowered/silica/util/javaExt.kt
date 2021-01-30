package org.sandboxpowered.silica.util

import org.apache.commons.lang3.StringUtils
import java.io.File

fun File.notExists(): Boolean = !exists()

fun File.deleteIfExists(): Boolean = exists().and(delete())

fun <T> Iterable<T>.join(separator: String): String = StringUtils.join(this, separator)