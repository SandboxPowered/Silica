package org.sandboxpowered.silica.network.login.serverbound;

import org.sandboxpowered.silica.network.*;

public class LoginStart implements Packet {
    private String username;

    @Override
    public void read(PacketByteBuf buf) {
        username = buf.readString(16);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {
        connection.handleLoginStart(username);
    }
}