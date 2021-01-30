package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

import java.util.ArrayList;
import java.util.List;

public class UpdateLight implements Packet {
    private int cX, cZ;
    private int skyYMask;
    private int blockYMask;
    private int emptySkyYMask;
    private int emptyBlockYMask;
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
        byte[] data = new byte[2048];
        for (int i = 0; i < 18; ++i) {
            this.emptySkyYMask |= 1 << i;
            this.emptyBlockYMask |= 1 << i;
        }
        int j;
        for (j = 0; j < 18; ++j) {
            if ((this.skyYMask & 1 << j) != 0) {
                System.out.println(true);
            }
        }
        for (j = 0; j < 18; ++j) {
            if ((this.blockYMask & 1 << j) != 0) {
                System.out.println(false);
            }
        }
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(cX);
        buf.writeVarInt(cZ);
        buf.writeBoolean(trustEdges);
        buf.writeVarInt(skyYMask);
        buf.writeVarInt(blockYMask);
        buf.writeVarInt(emptySkyYMask);
        buf.writeVarInt(emptyBlockYMask);

        for (byte[] skyUpdate : skyUpdates) {
            buf.writeByteArray(skyUpdate);
        }

        for (byte[] blockUpdate : blockUpdates) {
            buf.writeByteArray(blockUpdate);
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
