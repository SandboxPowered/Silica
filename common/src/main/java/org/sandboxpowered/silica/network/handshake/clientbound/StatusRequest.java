package org.sandboxpowered.silica.network.handshake.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.handshake.serverbound.StatusResponse;

public class StatusRequest implements Packet {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {
        packetHandler.sendPacket(new StatusResponse());
    }
}
