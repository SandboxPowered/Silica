package org.sandboxpowered.silica.util

import org.apache.commons.io.filefilter.IOFileFilter
import java.io.File

enum class FileFilters(private val extension: String) : IOFileFilter {
    ZIP("zip"), JAR("jar");

    override fun accept(file: File): Boolean = file.isFile && accept(file.parentFile, file.name)

    override fun accept(dir: File, name: String): Boolean = name.endsWith(".$extension")
}