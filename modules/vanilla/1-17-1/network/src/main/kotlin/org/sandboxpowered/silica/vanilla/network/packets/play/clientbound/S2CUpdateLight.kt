package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import java.util.*

class S2CUpdateLight : PacketPlay {
    private var cX = 0
    private var cZ = 0
    private val skyYMask = BitSet()
    private val blockYMask = BitSet()
    private val emptySkyYMask = BitSet()
    private val emptyBlockYMask = BitSet()
    private var skyUpdates: List<ByteArray>? = null
    private var blockUpdates: List<ByteArray>? = null
    private var trustEdges = false

    constructor()
    constructor(cX: Int, cZ: Int, trustEdges: Boolean) {
        this.cX = cX
        this.cZ = cZ
        this.trustEdges = trustEdges
        skyUpdates = ArrayList()
        blockUpdates = ArrayList()
        /*for (int i = 0; i < 34; ++i) {
            this.skyYMask.set(i, false);
            this.blockYMask.set(i);
            this.emptySkyYMask.set(i);
            this.emptyBlockYMask.set(i);
        }*/
    }

    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(cX)
        buf.writeVarInt(cZ)
        buf.writeBoolean(trustEdges)
        buf.writeLongArray(skyYMask.toLongArray())
        buf.writeLongArray(blockYMask.toLongArray())
        buf.writeLongArray(emptySkyYMask.toLongArray())
        buf.writeLongArray(emptyBlockYMask.toLongArray())
        buf.writeVarInt(skyUpdates!!.size)
        for (skyUpdate in skyUpdates!!) {
            buf.writeByteArray(skyUpdate)
        }
        buf.writeVarInt(blockUpdates!!.size)
        for (blockUpdate in blockUpdates!!) {
            buf.writeByteArray(blockUpdate)
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}