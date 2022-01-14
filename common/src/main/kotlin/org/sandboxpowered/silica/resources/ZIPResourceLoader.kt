package org.sandboxpowered.silica.resources

import org.sandboxpowered.utilities.Identifier
import java.io.File
import java.io.InputStream
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.zip.ZipFile

class ZIPResourceLoader(override val name: String, private val file: File) : AbstractResourceLoader() {
    private val zip: ZipFile = ZipFile(file)
    private var namespaces: Set<String>? = null

    override fun containsFile(file: String): Boolean = zip.getEntry(file) != null

    override fun openFile(file: String): InputStream = when (val zipEntry = zip.getEntry(file)) {
        null -> error("")
        else -> zip.getInputStream(zipEntry)
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
        var pattern: Pattern = Pattern.compile("(assets|data)\\/([a-z]*)\\/")
    }

}