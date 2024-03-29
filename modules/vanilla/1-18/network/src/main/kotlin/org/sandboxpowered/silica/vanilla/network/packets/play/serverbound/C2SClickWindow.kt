package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.readCollection
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.SlotData
import org.sandboxpowered.silica.vanilla.network.packets.play.readSlot

/**
 * See documentation at https://wiki.vg/Protocol#Click_Window
 */
data class C2SClickWindow(
    private val window: Byte,
    private val state: Int,
    private val slot: Short,
    private val button: Byte,
    private val mode: Int,
    private val slots: Int2ObjectMap<SlotData>,
    private val clickedItem: SlotData
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(
        buf.readByte(),
        buf.readVarInt(),
        buf.readShort(),
        buf.readByte(),
        buf.readVarInt(),
        buf.readCollection { it.readShort() to it.readSlot() }.let {
            Int2ObjectOpenHashMap<SlotData>(it.size).apply {
                it.forEach { (key, value) ->
                    put(key.toInt(), value)
                }
            }
        },
        buf.readSlot()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeByte(window)
        buf.writeByte(button)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle click window
    }
}