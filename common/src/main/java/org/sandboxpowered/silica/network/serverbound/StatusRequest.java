package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.clientbound.StatusResponse;
import org.sandboxpowered.silica.server.SilicaServer;

public class StatusRequest implements Packet {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, SilicaServer server) {
        packetHandler.sendPacket(new StatusResponse());
    }
}
