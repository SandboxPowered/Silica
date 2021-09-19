package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.util.Identifier
import java.io.File
import java.io.InputStream
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.zip.ZipFile

class ZIPResourceLoader(override val name: String, private val file: File) : ResourceLoader {
    private val zip: ZipFile
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