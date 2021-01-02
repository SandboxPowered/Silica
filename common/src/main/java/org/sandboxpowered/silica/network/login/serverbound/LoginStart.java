package org.sandboxpowered.silica.network.login.serverbound;

import org.sandboxpowered.silica.network.*;
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess;
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
    public void handle(PacketHandler packetHandler, Connection connection) {
        SilicaServer server = connection.getServer();
        connection.handleLoginStart(username);
//        packetHandler.sendPacket(new EncryptionRequest("", server.getKeyPair().getPublic().getEncoded(), server.getVerificationArray()));
        packetHandler.sendPacket(new LoginSuccess(connection.getProfile().getId(), username));
        packetHandler.setProtocol(Protocol.PLAY);
    }
}