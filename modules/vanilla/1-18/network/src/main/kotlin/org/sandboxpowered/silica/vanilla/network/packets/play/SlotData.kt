package org.sandboxpowered.silica.vanilla.network.packets.play

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.nbt.NBTCompound
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping

data class SlotData(
    val present: Boolean,
    val itemId: Int = 0,
    val itemCount: Byte = 0,
    val nbt: NBTCompound? = null
) {
    companion object {
        val EMPTY = SlotData(false)

        fun from(stack: ItemStack): SlotData =
            if (stack.isEmpty) EMPTY
            else SlotData(true, itemMapper[stack.item.identifier], stack.count.toByte())

        fun from(identifier: Identifier, count: Int = 1) =
            if (count == 0) EMPTY
            else SlotData(true, itemMapper[identifier], count.toByte())

        private val itemMapper by lazy { VanillaProtocolMapping.INSTANCE["minecraft:item"] }
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
