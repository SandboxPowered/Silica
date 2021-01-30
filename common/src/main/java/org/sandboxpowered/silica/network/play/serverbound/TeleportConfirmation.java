package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class TeleportConfirmation implements Packet {
    private int tpId;

    @Override
    public void read(PacketByteBuf buf) {
        tpId = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
