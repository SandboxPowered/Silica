package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.clientbound.PongResponse;
import org.sandboxpowered.silica.server.SilicaServer;

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
    public void handle(PacketHandler packetHandler, SilicaServer server) {
        packetHandler.sendPacket(new PongResponse(time));
    }
}
