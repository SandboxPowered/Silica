package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.server.SilicaServer;

public class EncryptionResponse implements Packet {
    private byte[] sharedSecret;
    private byte[] verifyToken;

    @Override
    public void read(PacketByteBuf buf) {
        sharedSecret = buf.readByteArray();
        verifyToken = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByteArray(sharedSecret);
        buf.writeByteArray(verifyToken);
    }

    @Override
    public void handle(PacketHandler packetHandler, SilicaServer server) {

    }
}
