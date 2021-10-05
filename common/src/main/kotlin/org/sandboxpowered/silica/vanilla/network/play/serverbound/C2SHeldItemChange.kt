package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SHeldItemChange(private val slot: Int) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readShort().toInt())

    override fun write(buf: PacketByteBuf) {
        buf.writeShort(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayerInventory {
            if (slot in 0..8)
                it.selectedSlot = slot
            println("Selecting $slot")
        }
    }
}