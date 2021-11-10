package org.sandboxpowered.silica.resources

import org.sandboxpowered.silica.resources.ResourceLoader.Companion.getPath
import org.sandboxpowered.silica.api.util.Identifier
import java.io.InputStream

abstract class AbstractResourceLoader : ResourceLoader {
    override fun open(file: String): InputStream {
        return if ("/" !in file && "\\" !in file) this.openFile(file)
        else throw IllegalArgumentException("Root resources can only be a single filename")
    }

    override fun open(type: ResourceType, file: Identifier): InputStream = openFile(getPath(type, file))

    override fun contains(type: ResourceType, file: Identifier): Boolean = containsFile(getPath(type, file))

    protected abstract fun openFile(file: String): InputStream
    protected abstract fun containsFile(file: String): Boolean
}