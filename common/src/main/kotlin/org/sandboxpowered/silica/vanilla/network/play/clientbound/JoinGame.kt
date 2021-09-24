package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class JoinGame(
    private var playerId: Int,
    private var hardcore: Boolean,
    private var gamemode: Byte,
    private var previousGamemode: Byte,
    private var worldCount: Int,
    private var worldNames: Array<Identifier> = emptyArray(),
    private var dimCodec: NBTCompound?,
    private var dim: NBTCompound?,
    private var world: Identifier,
    private var seed: Long,
    private var maxPlayers: Int,
    private var viewDistance: Int,
    private var reducedDebug: Boolean,
    private var respawnScreen: Boolean,
    private var debug: Boolean,
    private var flat: Boolean,
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