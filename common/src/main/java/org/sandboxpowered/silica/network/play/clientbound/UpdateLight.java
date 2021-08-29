package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;
import org.sandboxpowered.silica.network.util.BitPackedLongArray;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class UpdateLight implements PacketPlay {
    private int cX, cZ;
    private BitSet skyYMask = new BitSet();
    private BitSet blockYMask = new BitSet();
    private BitSet emptySkyYMask = new BitSet();
    private BitSet emptyBlockYMask = new BitSet();
    private List<byte[]> skyUpdates;
    private List<byte[]> blockUpdates;
    private boolean trustEdges;

    public UpdateLight() {
    }

    public UpdateLight(int cX, int cZ, boolean trustEdges) {
        this.cX = cX;
        this.cZ = cZ;
        this.trustEdges = trustEdges;
        this.skyUpdates = new ArrayList<>();
        this.blockUpdates = new ArrayList<>();
        /*for (int i = 0; i < 34; ++i) {
            this.skyYMask.set(i, false);
            this.blockYMask.set(i);
            this.emptySkyYMask.set(i);
            this.emptyBlockYMask.set(i);
        }*/
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(cX);
        buf.writeVarInt(cZ);
        buf.writeBoolean(trustEdges);
        buf.writeLongArray(skyYMask.toLongArray());
        buf.writeLongArray(blockYMask.toLongArray());
        buf.writeLongArray(emptySkyYMask.toLongArray());
        buf.writeLongArray(emptyBlockYMask.toLongArray());

        buf.writeVarInt(skyUpdates.size());
        for (byte[] skyUpdate : skyUpdates) {
            buf.writeByteArray(skyUpdate);
        }

        buf.writeVarInt(blockUpdates.size());
        for (byte[] blockUpdate : blockUpdates) {
            buf.writeByteArray(blockUpdate);
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
