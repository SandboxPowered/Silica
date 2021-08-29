package org.sandboxpowered.silica.util

import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.nio.IntBuffer
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

fun File.listFiles(filter: FileFilter, function: (File) -> Unit) {
    listFiles(filter)?.forEach(function)
}

fun File.listFiles(filter: FilenameFilter, function: (File) -> Unit) {
    listFiles(filter)?.forEach(function)
}

fun File.listFiles(filter: IOFileFilter, function: (File) -> Unit) {
    listFiles(filter as FileFilter)?.forEach(function)
}

fun File.listFiles(filter: IOFileFilter): Array<out File>? = listFiles(filter as FileFilter)

fun <T> T?.ifPresent(consumer: (T) -> Unit) {
    if (this != null)
        consumer.invoke(this)
}

operator fun IntBuffer.set(index: Int, value: Int): IntBuffer = put(index, value)
