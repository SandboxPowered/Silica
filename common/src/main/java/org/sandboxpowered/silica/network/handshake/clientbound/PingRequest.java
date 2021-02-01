package org.sandboxpowered.silica.network.handshake.clientbound;

import org.sandboxpowered.silica.network.*;
import org.sandboxpowered.silica.network.handshake.serverbound.PongResponse;

public class PingRequest implements Packet {
    private long time;

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
        packetHandler.sendPacket(new PongResponse(time));
    }
}
