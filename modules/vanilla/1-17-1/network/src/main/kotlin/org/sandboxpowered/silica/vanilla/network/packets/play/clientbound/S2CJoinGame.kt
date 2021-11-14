package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.nbt.NBTCompound
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.readCollection
import org.sandboxpowered.silica.api.network.writeCollection
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CJoinGame(
    private val playerId: Int,
    private val hardcore: Boolean,
    private val gamemode: Byte,
    private val previousGamemode: Byte,
    private val worldNames: Collection<Identifier>,
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
    constructor(buf: PacketBuffer) : this(
        buf.readInt(),
        buf.readBoolean(),
        buf.readByte(),
        buf.readByte(),
        buf.readCollection(PacketBuffer::readIdentifier),
        buf.readNBT(),
        buf.readNBT(),
        buf.readIdentifier(),
        buf.readLong(),
        buf.readVarInt(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeInt(playerId)
        buf.writeBoolean(hardcore)
        buf.writeByte(gamemode)
        buf.writeByte(previousGamemode)
        buf.writeCollection(worldNames, PacketBuffer::writeIdentifier)
        buf.writeNBT(dimCodec)
        buf.writeNBT(dim)
        buf.writeIdentifier(world)
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