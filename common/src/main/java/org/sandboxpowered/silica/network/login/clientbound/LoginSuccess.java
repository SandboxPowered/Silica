package org.sandboxpowered.silica.network.login.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

import java.util.UUID;

public class LoginSuccess implements Packet {
    private UUID uuid;
    private String username;

    public LoginSuccess(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public LoginSuccess() {
    }

    @Override
    public void read(PacketByteBuf buf) {
        uuid = buf.readUUID();
        username = buf.readString(16);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
