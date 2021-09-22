package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.util.Identifier

class JoinGame(
    private var playerId: Int = 0, private var hardcore: Boolean = false, private var gamemode: Short = 0,
    private var previousGamemode: Short = 0, private var worldCount: Int = 0,
    private var worldNames: Array<Identifier> = emptyArray(), private var dimCodec: NBTCompound? = null,
    private var dim: NBTCompound? = null, private var world: Identifier? = null, private var seed: Long = 0,
    private var maxPlayers: Int = 0, private var viewDistance: Int = 0,
    private var reducedDebug: Boolean = false, private var respawnScreen: Boolean = false,
    private var debug: Boolean = false, private var flat: Boolean = false,
) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        playerId = buf.readInt()
        hardcore = buf.readBoolean()
        gamemode = buf.readByte().toShort()
        previousGamemode = buf.readByte().toShort()
        worldCount = buf.readVarInt()
        worldNames = buf.readIdentityArray()
        dimCodec = buf.readNBT()
        dim = buf.readNBT()
        world = buf.readIdentity()
        seed = buf.readLong()
        maxPlayers = buf.readVarInt()
        viewDistance = buf.readVarInt()
        reducedDebug = buf.readBoolean()
        respawnScreen = buf.readBoolean()
        debug = buf.readBoolean()
        flat = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(playerId)
        buf.writeBoolean(hardcore)
        buf.writeByte(gamemode.toInt())
        buf.writeByte(previousGamemode.toInt())
        buf.writeVarInt(worldCount)
        buf.writeIdentityArray(worldNames)
        buf.writeNBT(dimCodec)
        buf.writeNBT(dim)
        buf.writeIdentity(world!!)
        buf.writeLong(seed)
        buf.writeVarInt(maxPlayers)
        buf.writeVarInt(viewDistance)
        buf.writeBoolean(reducedDebug)
        buf.writeBoolean(respawnScreen)
        buf.writeBoolean(debug)
        buf.writeBoolean(flat)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}