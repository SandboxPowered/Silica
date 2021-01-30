package org.sandboxpowered.silica.util

import org.apache.commons.lang3.StringUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

fun File.notExists(): Boolean = !exists()

fun File.deleteIfExists(): Boolean = exists().and(delete())

fun <T> Iterable<T>.join(separator: String): String = StringUtils.join(this, separator)

fun <T> Class<T>.getResourceAsString(path: String): String {
    val result: String
    getResourceAsStream(path).use { stream ->
        Scanner(stream, StandardCharsets.UTF_8).use {
            result = it.useDelimiter("\\A").next()
        }
    }
    return result
}