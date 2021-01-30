package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class KeepAliveServer implements Packet {
    private long id;

    public KeepAliveServer() {
    }

    public KeepAliveServer(long id) {
        this.id = id;
    }

    @Override
    public void read(PacketByteBuf buf) {
        id = buf.readLong();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(id);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
