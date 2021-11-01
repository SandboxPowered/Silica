package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.play.SlotData

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
        buf.readCollection(SlotData::invoke),
        SlotData(buf)
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeUByte(window)
        buf.writeVarInt(state)
        buf.writeCollection(slots, SlotData::write)
        cursorStack.write(buf)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}