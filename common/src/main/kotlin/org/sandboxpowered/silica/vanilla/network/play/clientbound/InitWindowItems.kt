package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.content.inventory.PlayerInventory.Companion.MAIN_SIZE
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class InitWindowItems(
    val window: UByte,
    val state: Int,
    val inventory: PlayerInventory?,
    val protocolMapping: VanillaProtocolMapping?
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readUByte(), buf.readVarInt(), null, null)

    override fun write(buf: PacketByteBuf) {
        val inventory = this.inventory!!
        buf.writeUByte(window)
        buf.writeVarInt(state)
        buf.writeVarInt(MAIN_SIZE)
        for (i in 0 until MAIN_SIZE) {
            writeStack(buf, inventory[i])
        }
        writeStack(buf, inventory[inventory.selectedSlot])
    }

    private fun writeStack(buf: PacketByteBuf, stack: ItemStack) {
        if (stack.isEmpty) {
            buf.writeBoolean(false) // Empty item, dont bother sending anything
        } else {
            buf.writeBoolean(true) // Has Item
            buf.writeVarInt(protocolMapping!!["minecraft:item"]!![stack.item.identifier]) // Item ID
            buf.writeByte(stack.count) // Stack Count
            buf.writeByte(0) // NBT
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}