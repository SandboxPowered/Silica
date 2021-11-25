package org.sandboxpowered.silica.vanilla.network.util.extensions

import org.sandboxpowered.silica.api.network.PacketBuffer

fun PacketBuffer.readAngle(): Float {
    return readByte() * 360f / 256f
}

fun PacketBuffer.writeAngle(angle: Float): PacketBuffer {
    writeByte((angle * 256f / 360f).toInt().toByte())
    return this
}