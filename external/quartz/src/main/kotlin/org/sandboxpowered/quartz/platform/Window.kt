package org.sandboxpowered.quartz.platform

import org.sandboxpowered.quartz.api.Destructible

interface Window : Destructible {
    val handle: Long

    var title: String

    val width: Int
    val height: Int

    val fps: Int

    var visible: Boolean

    val shouldClose: Boolean

    fun close()
}