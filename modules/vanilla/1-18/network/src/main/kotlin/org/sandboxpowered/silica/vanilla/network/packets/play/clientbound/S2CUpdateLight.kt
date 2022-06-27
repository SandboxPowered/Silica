package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import java.util.*

class S2CUpdateLight(
    private val cX: Int,
    private val cZ: Int,
    private val trustEdges: Boolean,
    private val skyUpdates: List<ByteArray>,
    private val blockUpdates: List<ByteArray>
) : PacketPlay {
    private val skyYMask = BitSet()
    private val blockYMask = BitSet()
    private val emptySkyYMask = BitSet()
    private val emptyBlockYMask = BitSet()

    init {
        /*for (int i = 0; i < 34; ++i) {
            this.skyYMask.set(i, false);
            this.blockYMask.set(i);
            this.emptySkyYMask.set(i);
            this.emptyBlockYMask.set(i);
        }*/
    }

    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readBoolean(),
        TODO(),
        TODO()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(cX)
        buf.writeVarInt(cZ)
        buf.writeBoolean(trustEdges)
        buf.writeLongArray(skyYMask.toLongArray())
        buf.writeLongArray(blockYMask.toLongArray())
        buf.writeLongArray(emptySkyYMask.toLongArray())
        buf.writeLongArray(emptyBlockYMask.toLongArray())
        buf.writeVarInt(skyUpdates.size)
        for (skyUpdate in skyUpdates) {
            buf.writeByteArray(skyUpdate)
        }
        buf.writeVarInt(blockUpdates.size)
        for (blockUpdate in blockUpdates) {
            buf.writeByteArray(blockUpdate)
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}