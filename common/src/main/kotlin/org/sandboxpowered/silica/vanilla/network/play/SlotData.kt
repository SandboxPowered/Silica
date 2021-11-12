package org.sandboxpowered.silica.vanilla.network.play

import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.nbt.NBTCompound
import org.sandboxpowered.silica.api.network.PacketBuffer

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

fun PacketBuffer.writeSlot(slot: SlotData): PacketBuffer {
    if (slot.present) {
        writeBoolean(true)
        writeVarInt(slot.itemId)
        writeByte(slot.itemCount)
        writeNBT(slot.nbt)
    } else writeBoolean(false)
    return this
}

fun PacketBuffer.readSlot(): SlotData =
    if (readBoolean()) {
        val itemId = readVarInt()
        val itemCount = readByte()
        val nbt = readNBT()
        SlotData(true, itemId, itemCount, nbt)
    } else SlotData.EMPTY
