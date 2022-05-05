package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CEntityMetadata(
    private val entityId: Int,
    private val metadata: Iterable<Entry>
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buildList<Entry> {
            var index = buf.readUByte()
            while (0xffu.toUByte() != index) {
                val type = buf.readVarInt()
                // TODO: read value
                index = buf.readUByte()
            }
        }
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        metadata.forEach {
            buf.writeUByte(it.index)
            buf.writeVarInt(it.type)
            it.value(buf)
        }
        buf.writeUByte(0xffu)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}

    class Entry private constructor(
        val index: UByte,
        val type: Int,
        val value: (PacketBuffer) -> Any
    ) {
        companion object {
            private val chatSerializer = GsonComponentSerializer.gson()

            fun customName(name: String) = Entry(2u, 5) { buf ->
                buf.writeBoolean(true).writeString(
                    chatSerializer.serialize(Component.text(name))
                )
            }

            fun customNameVisible(visible: Boolean) = Entry(3u, 7) { it.writeBoolean(visible) }
        }
    }
}