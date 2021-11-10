package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.resources.ResourceLoader.Companion.isValidPath
import org.sandboxpowered.silica.api.util.Identifier
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.util.function.Predicate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ClasspathResourceLoader(override val name: String, var namespaces: Array<String>) : ResourceLoader {

    private fun findInputStream(type: ResourceType, file: Identifier): InputStream? {
        val pathString = getPath(type, file)

        val url = ClasspathResourceLoader::class.java.getResource(pathString)
        if (isValidUrl(pathString, url)) {
            return url.openStream()
        }
        return ClasspathResourceLoader::class.java.getResourceAsStream(pathString)
    }

    @OptIn(ExperimentalContracts::class)
    private fun isValidUrl(fileName: String, url: URL?): Boolean {
        contract { returns(true) implies (url != null) }
        return url != null && (url.protocol == "jar" || isValidPath(File(url.file), fileName))
    }

    private fun getPath(type: ResourceType, identifier: Identifier): String {
        return "/${type.folder}/${identifier.namespace}/${identifier.path}"
    }

    override fun contains(type: ResourceType, file: Identifier): Boolean {
        val path = getPath(type, file)
        val url = ClasspathResourceLoader::class.java.getResource(path)
        return isValidUrl(path, url)
    }

    override fun open(file: String): InputStream {
        if (!file.contains('/') && !file.contains('\\'))
            return ClasspathResourceLoader::class.java.getResourceAsStream("/$file")!!
        else throw IllegalArgumentException("Root resources can only be a single filename")
    }

    override fun open(type: ResourceType, file: Identifier): InputStream {
        when (val stream = findInputStream(type, file)) {
            null -> throw FileNotFoundException(file.path)
            else -> return stream
        }
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
        return setOf(*namespaces)
    }
}