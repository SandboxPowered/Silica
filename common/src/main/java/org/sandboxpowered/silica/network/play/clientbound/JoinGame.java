package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.nbt.CompoundTag;
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

    public JoinGame() {
    }

    public JoinGame(int playerId, boolean hardcore, short gamemode, short previousGamemode, int worldCount, Identity[] worldNames, CompoundTag dimCodec, CompoundTag dim, Identity world, long seed, int maxPlayers, int viewDistance, boolean reducedDebug, boolean respawnScreen, boolean debug, boolean flat) {
        this.playerId = playerId;
        this.hardcore = hardcore;
        this.gamemode = gamemode;
        this.previousGamemode = previousGamemode;
        this.worldCount = worldCount;
        this.worldNames = worldNames;
        this.dimCodec = dimCodec;
        this.dim = dim;
        this.world = world;
        this.seed = seed;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.reducedDebug = reducedDebug;
        this.respawnScreen = respawnScreen;
        this.debug = debug;
        this.flat = flat;
    }

    @Override
    public void read(PacketByteBuf buf) {
        playerId = buf.readInt();
        hardcore = buf.readBoolean();
        gamemode = buf.readByte();
        previousGamemode = buf.readByte();
        worldCount = buf.readVarInt();
        worldNames = buf.readIdentityArray();
        dimCodec = buf.readNBT();
        dim = buf.readNBT();
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
        buf.writeInt(playerId);
        buf.writeBoolean(hardcore);
        buf.writeByte(gamemode);
        buf.writeByte(previousGamemode);
        buf.writeVarInt(worldCount);
        buf.writeIdentityArray(worldNames);
        buf.writeNBT(dimCodec);
        buf.writeNBT(dim);
        buf.writeIdentity(world);
        buf.writeLong(seed);
        buf.writeVarInt(maxPlayers);
        buf.writeVarInt(viewDistance);
        buf.writeBoolean(reducedDebug);
        buf.writeBoolean(respawnScreen);
        buf.writeBoolean(debug);
        buf.writeBoolean(flat);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}