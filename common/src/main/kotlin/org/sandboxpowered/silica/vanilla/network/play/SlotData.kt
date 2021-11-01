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

    fun write(buf: PacketByteBuf): PacketByteBuf {
        if (present) {
            buf.writeBoolean(true)
            buf.writeVarInt(itemId)
            buf.writeByte(itemCount)
            buf.writeNBT(nbt)
        } else buf.writeBoolean(false)
        return buf
    }

    companion object {
        val EMPTY = SlotData(false)

        fun from(stack: ItemStack, mapper: (Item) -> Int): SlotData =
            if (stack.isEmpty) EMPTY
            else SlotData(true, mapper(stack.item), stack.count.toByte())

        operator fun invoke(buf: PacketByteBuf): SlotData =
            if (buf.readBoolean()) {
                val itemId = buf.readVarInt()
                val itemCount = buf.readByte()
                val nbt = buf.readNBT()
                SlotData(true, itemId, itemCount, nbt)
            } else EMPTY
    }
}