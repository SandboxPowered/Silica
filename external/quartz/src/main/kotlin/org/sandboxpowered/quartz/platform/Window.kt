package org.sandboxpowered.quartz.platform

interface Window {
    val handle: Long

    val width: Int
    val height: Int

    val fps: Int
}