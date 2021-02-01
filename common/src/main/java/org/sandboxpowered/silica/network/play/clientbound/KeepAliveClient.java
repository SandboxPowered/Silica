package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class KeepAliveClient implements PacketPlay {
    private long id;

    public KeepAliveClient() {
    }

    public KeepAliveClient(long id) {
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
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
