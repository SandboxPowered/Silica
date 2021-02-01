package org.sandboxpowered.silica.network.handshake.serverbound;

import org.sandboxpowered.silica.network.*;

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
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}