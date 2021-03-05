package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;

public class ClientPluginChannel implements PacketPlay {
    private Identity channel;
    private byte[] data;

    @Override
    public void read(PacketByteBuf buf) {
        channel = buf.readIdentity();
        data = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
