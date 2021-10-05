package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CJoinGame(
    private val playerId: Int,
    private val hardcore: Boolean,
    private val gamemode: Byte,
    private val previousGamemode: Byte,
    private val worldCount: Int,
    private val worldNames: Array<Identifier> = emptyArray(),
    private val dimCodec: NBTCompound?,
    private val dim: NBTCompound?,
    private val world: Identifier,
    private val seed: Long,
    private val maxPlayers: Int,
    private val viewDistance: Int,
    private val reducedDebug: Boolean,
    private val respawnScreen: Boolean,
    private val debug: Boolean,
    private val flat: Boolean,
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(
        buf.readInt(),
        buf.readBoolean(),
        buf.readByte(),
        buf.readByte(),
        buf.readVarInt(),
        buf.readIdentityArray(),
        buf.readNBT(),
        buf.readNBT(),
        buf.readIdentity(),
        buf.readLong(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readBoolean()
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(playerId)
        buf.writeBoolean(hardcore)
        buf.writeByte(gamemode.toInt())
        buf.writeByte(previousGamemode.toInt())
        buf.writeVarInt(worldCount)
        buf.writeIdentityArray(worldNames)
        buf.writeNBT(dimCodec)
        buf.writeNBT(dim)
        buf.writeIdentity(world)
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