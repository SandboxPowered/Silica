package org.sandboxpowered.silica

import java.io.File

fun File.notExists(): Boolean {
    return !exists()
}
fun File.deleteIfExists(): Boolean {
    if (exists())
        return delete()
    return false
}