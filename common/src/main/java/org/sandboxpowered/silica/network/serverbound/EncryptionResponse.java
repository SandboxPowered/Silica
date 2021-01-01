package org.sandboxpowered.silica.network.serverbound;

import org.apache.logging.log4j.LogManager;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

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

    }

    @Override
    public void handle(PacketHandler packetHandler) {
        LogManager.getLogger().info("Got encryption response");
    }
}
