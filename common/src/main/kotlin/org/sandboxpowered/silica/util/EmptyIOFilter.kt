package org.sandboxpowered.silica.util

import org.apache.commons.io.filefilter.IOFileFilter
import java.io.File

class EmptyIOFilter : IOFileFilter {
    override fun accept(file: File): Boolean = false

    override fun accept(dir: File, name: String): Boolean = false
}