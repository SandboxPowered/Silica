package org.sandboxpowered.silica.resources

import java.io.File
import java.io.InputStream
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.zip.ZipFile

class ZIPResourceLoader(private val file: File) : ResourceLoader {
    private val zip: ZipFile
    private var namespaces: Set<String>? = null
    override fun containsFile(type: ResourceType, path: String): Boolean {
        return false
    }

    override fun openFile(type: ResourceType, path: String): InputStream {
        TODO("Not yet implemented")
    }

    override fun findResources(
        type: ResourceType,
        namespace: String,
        path: String,
        depth: Int,
        filter: Predicate<String>
    ): Set<String> {
        return emptySet()
    }

    override fun getNamespaces(type: ResourceType): Set<String> {
        if (namespaces == null) {
            val enumeration = zip.entries()
            val namespaces = HashSet<String>()
            while (enumeration.hasMoreElements()) {
                val zipEntry = enumeration.nextElement()
                val string = zipEntry.name
                if (string.startsWith(type.folder + '/')) {
                    val matcher = pattern.matcher(string)
                    if (matcher.find()) {
                        namespaces.add(matcher.group(2))
                    }
                }
            }
            this.namespaces = namespaces
        }
        return namespaces!!
    }

    companion object {
        var pattern = Pattern.compile("(assets|data)\\/([a-z]*)\\/")
    }

    init {
        zip = ZipFile(file)
    }
}