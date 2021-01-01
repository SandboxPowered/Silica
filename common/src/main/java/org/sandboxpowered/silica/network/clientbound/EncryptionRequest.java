package org.sandboxpowered.silica.network.clientbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.server.SilicaServer;

public class EncryptionRequest implements Packet {
    private String serverId;
    private byte[] publicKey;
    private byte[] verifyArray;

    public EncryptionRequest() {
    }

    public EncryptionRequest(String serverId, byte[] publicKey, byte[] verifyArray) {
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyArray = verifyArray;
    }

    @Override
    public void read(PacketByteBuf buf) {
        serverId = buf.readString(20);
        publicKey = buf.readByteArray();
        verifyArray = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.serverId);
        buf.writeByteArray(this.publicKey);
        buf.writeByteArray(this.verifyArray);
    }

    @Override
    public void handle(PacketHandler packetHandler, SilicaServer server) {

    }
}
