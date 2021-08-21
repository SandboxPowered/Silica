package org.sandboxpowered.silica.util

import org.apache.commons.io.filefilter.IOFileFilter
import java.io.File

enum class FileFilters(private val extension: String) : IOFileFilter {
    ZIP("zip"), JAR("jar");

    override fun accept(file: File): Boolean {
        return accept(file.parentFile, file.name)
    }

    override fun accept(dir: File, name: String): Boolean {
        return name.endsWith(".$extension")
    }

    fun or(other: IOFileFilter): IOFileFilter {
        return object : IOFileFilter {
            override fun accept(file: File): Boolean {
                return this@FileFilters.accept(file) || other.accept(file)
            }

            override fun accept(dir: File, name: String): Boolean {
                return this@FileFilters.accept(dir, name) || other.accept(dir, name)
            }
        }
    }
}