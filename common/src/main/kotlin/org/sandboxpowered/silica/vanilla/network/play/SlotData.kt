package org.sandboxpowered.silica.vanilla.network.play

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf

data class SlotData(
    val present: Boolean,
    val itemId: Int = 0,
    val itemCount: Byte = 0,
    val nbt: NBTCompound? = null
) {
    companion object {
        val EMPTY = SlotData(false)

        fun from(stack: ItemStack, mapper: (Item) -> Int): SlotData =
            if (stack.isEmpty) EMPTY
            else SlotData(true, mapper(stack.item), stack.count.toByte())
    }
}

fun PacketByteBuf.writeSlot(slot: SlotData): PacketByteBuf {
    if (slot.present) {
        writeBoolean(true)
        writeVarInt(slot.itemId)
        writeByte(slot.itemCount)
        writeNBT(slot.nbt)
    } else writeBoolean(false)
    return this
}

fun PacketByteBuf.readSlot(): SlotData =
    if (readBoolean()) {
        val itemId = readVarInt()
        val itemCount = readByte()
        val nbt = readNBT()
        SlotData(true, itemId, itemCount, nbt)
    } else SlotData.EMPTY
