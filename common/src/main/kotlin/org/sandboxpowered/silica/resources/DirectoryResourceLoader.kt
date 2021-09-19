package org.sandboxpowered.silica.resources

import com.google.common.collect.Sets
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.sandboxpowered.silica.util.Identifier
import java.io.File
import java.io.FileFilter
import java.io.InputStream
import java.util.function.Predicate

class DirectoryResourceLoader(override val name: String, private val directory: File) : ResourceLoader {
    private var namespaces: Set<String>? = null

    override fun contains(type: ResourceType, file: Identifier): Boolean {
        TODO("Not yet implemented")
    }

    override fun open(file: String): InputStream? {
        TODO("Not yet implemented")
    }

    override fun open(type: ResourceType, file: Identifier): InputStream {
        TODO("Not yet implemented")
    }

    override fun findResources(
        type: ResourceType,
        namespace: String,
        path: String,
        depth: Int,
        filter: Predicate<String>
    ): Set<Identifier> {
        TODO("Not yet implemented")
    }

    override fun getNamespaces(type: ResourceType): Set<String> {
        return if (namespaces != null) {
            namespaces!!
        } else {
            val set: MutableSet<String> = Sets.newHashSet()
            val file = File(directory, "assets")
            val files = file.listFiles(DirectoryFileFilter.DIRECTORY as FileFilter)
            if (files != null) {
                for (file2 in files) {
                    val string = relativize(file, file2)
                    set.add(string.substring(0, string.length - 1))
                }
            }
            namespaces = set
            set
        }
    }

    companion object {
        protected fun relativize(file: File, file2: File): String {
            return file.toURI().relativize(file2.toURI()).path
        }
    }
}