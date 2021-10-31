package org.sandboxpowered.silica.vanilla.network.play

import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf

data class SlotData(
    val present: Boolean,
    val itemId: Int = 0,
    val itemCount: Byte = 0,
    val nbt: NBTCompound? = null
) {

    fun write(buf: PacketByteBuf) {
        if (present) {
            buf.writeBoolean(true)
            buf.writeInt(itemId)
            buf.writeNBT(nbt)
        } else buf.writeBoolean(false)
    }

    companion object {
        operator fun invoke(buf: PacketByteBuf): SlotData {
            val present = buf.readBoolean()
            return if (present) {
                val itemId = buf.readVarInt()
                val itemCount = buf.readByte()
                val nbt = buf.readNBT()
                SlotData(true, itemId, itemCount, nbt)
            } else SlotData(false)
        }
    }
}