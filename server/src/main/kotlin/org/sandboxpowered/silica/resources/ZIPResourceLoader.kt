package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.api.util.Identifier
import java.io.File
import java.io.InputStream
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.zip.ZipFile

class ZIPResourceLoader(override val name: String, file: File) : AbstractResourceLoader() {
    private val zip: ZipFile = ZipFile(file)
    private var namespaces: Set<String>? = null

    override fun containsFile(file: String): Boolean = zip.getEntry(file) != null

    override fun openFile(file: String): InputStream = when (val zipEntry = zip.getEntry(file)) {
        null -> error("")
        else -> zip.getInputStream(zipEntry)
    }

    override fun findResources(
        type: ResourceType,
        namespace: String?,
        category: String?,
        depth: Int,
        filter: Predicate<String>
    ): Set<Identifier> = buildSet {
        for (entry in zip.entries()) {
            val matcher = pattern.matcher(entry.name)
            if (matcher.find()
                && matcher.group(TYPE_GROUP) == type.folder
            ) {
                val fileNameSpace = matcher.group(NAMESPACE_GROUP)
                val fileCategory = matcher.group(CATEGORY_GROUP)
                val path = matcher.group(FILE_GROUP)
                if (
                    (namespace == null || fileNameSpace == namespace)
                    && (category == null || fileCategory == category)
                    && (depth < 0 || path.count { it == '/' } <= depth)
                    && filter.test(path)
                ) {
                    this += Identifier(fileNameSpace, "$fileCategory/$path")
                }
            }
        }
    }

    override fun getNamespaces(type: ResourceType): Set<String> {
        if (namespaces == null) {
            val enumeration = zip.entries()
            val namespaces = HashSet<String>()
            while (enumeration.hasMoreElements()) {
                val zipEntry = enumeration.nextElement()
                val string = zipEntry.name
                if (string.startsWith(type.folder + '/')) {
                    val matcher = oldPattern.matcher(string)
                    if (matcher.find()) {
                        namespaces.add(matcher.group(2))
                    }
                }
            }
            this.namespaces = namespaces
        }
        return namespaces!!
    }

    private companion object {
        private val oldPattern: Pattern = Pattern.compile("(assets|data)\\/([a-z]*)\\/")
        private const val TYPE_GROUP = "TYP"
        private const val NAMESPACE_GROUP = "NS"
        private const val CATEGORY_GROUP = "CAT"
        private const val FILE_GROUP = "FILE"
        private val pattern: Pattern = Pattern.compile(
            "(?<$TYPE_GROUP>[a-z]+)" +
                    "/(?<$NAMESPACE_GROUP>[a-z]+)" +
                    "(?:/(?<$CATEGORY_GROUP>[a-z]+))?" +
                    "/(?<$FILE_GROUP>[a-z._/]+)"
        )
    }

}