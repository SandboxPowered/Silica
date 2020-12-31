package org.sandboxpowered.silica.network.clientbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class PongResponse implements Packet {
    private long time;

    public PongResponse(long time) {
        this.time = time;
    }

    public PongResponse() {
    }

    @Override
    public void read(PacketByteBuf buf) {
        this.time = buf.readLong();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(this.time);
    }

    @Override
    public void handle(PacketHandler packetHandler) {

    }
}