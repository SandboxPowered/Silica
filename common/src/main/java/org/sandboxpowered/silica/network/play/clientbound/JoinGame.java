package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.api.util.nbt.CompoundTag;
import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class JoinGame implements Packet {
    private int playerId;
    private boolean hardcore;
    private short gamemode;
    private short previousGamemode;
    private int worldCount;
    private Identity[] worldNames;
    private CompoundTag dimCodec;
    private CompoundTag dim;
    private Identity world;
    private long seed;
    private int maxPlayers;
    private int viewDistance;
    private boolean reducedDebug;
    private boolean respawnScreen;
    private boolean debug;
    private boolean flat;

    @Override
    public void read(PacketByteBuf buf) {
        playerId = buf.readInt();
        hardcore = buf.readBoolean();
        gamemode = buf.readUnsignedByte();
        previousGamemode = buf.readUnsignedByte();
        worldCount = buf.readVarInt();
        worldNames = buf.readIdentityArray();
        // TODO
        // dimCodec
        // dim
        world = buf.readIdentity();
        seed = buf.readLong();
        maxPlayers = buf.readVarInt();
        viewDistance = buf.readVarInt();
        reducedDebug = buf.readBoolean();
        respawnScreen = buf.readBoolean();
        debug = buf.readBoolean();
        flat = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
