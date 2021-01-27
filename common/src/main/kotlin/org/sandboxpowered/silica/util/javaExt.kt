package org.sandboxpowered.silica.util

import java.io.File

fun File.notExists(): Boolean = !exists()

fun File.deleteIfExists(): Boolean = exists().and(delete())