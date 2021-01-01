package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.clientbound.EncryptionRequest;
import org.sandboxpowered.silica.server.SilicaServer;

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
    public void handle(PacketHandler packetHandler, SilicaServer server) {
        packetHandler.sendPacket(new EncryptionRequest("", server.getKeyPair().getPublic().getEncoded(), server.getVerificationArray()));
    }
}