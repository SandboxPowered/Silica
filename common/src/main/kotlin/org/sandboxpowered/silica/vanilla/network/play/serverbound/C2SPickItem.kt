package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPickItem(val slot: Int) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readVarInt())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayerInventory {
            it[it.selectedSlot] = ItemStack(PlayerInventory.STONE)
        }
    }
}