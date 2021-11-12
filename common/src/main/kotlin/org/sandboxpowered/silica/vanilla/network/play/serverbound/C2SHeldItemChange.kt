package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SHeldItemChange(private val slot: Short) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readShort())

    override fun write(buf: PacketBuffer) {
        buf.writeShort(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayerInventory {
            it.selectedSlot = slot.toInt()
        }
    }
}