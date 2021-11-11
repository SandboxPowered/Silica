package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.inventory.PlayerInventory
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.play.SlotData
import org.sandboxpowered.silica.vanilla.network.play.readSlot
import org.sandboxpowered.silica.vanilla.network.play.writeSlot

class S2CInitWindowItems(
    private val window: UByte,
    private val state: Int,
    private val slots: Collection<SlotData>,
    private val cursorStack: SlotData
) : PacketPlay {
    constructor(
        window: UByte,
        state: Int,
        inventory: PlayerInventory,
        protocolMapping: (Item) -> Int
    ) : this(window, state, inventory.map { SlotData.from(it, protocolMapping) }, SlotData.EMPTY)

    constructor(buf: PacketByteBuf) : this(
        buf.readUByte(),
        buf.readVarInt(),
        buf.readCollection(PacketByteBuf::readSlot),
        buf.readSlot()
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeUByte(window)
        buf.writeVarInt(state)
        buf.writeCollection(slots, PacketByteBuf::writeSlot)
        buf.writeSlot(cursorStack)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}