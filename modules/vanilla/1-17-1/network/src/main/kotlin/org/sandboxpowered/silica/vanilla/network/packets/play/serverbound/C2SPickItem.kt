package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPickItem(val slot: Int) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt())

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayerInventory {
            // TODO: find block player is looking at and add it to inventory at selected slot or pick slot if it exists in that slot.
//            it[it.selectedSlot] = ItemStack(Items.STONE)
        }
    }
}