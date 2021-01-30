package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class UpdateChunkPosition implements Packet {
    private int x, y;

    public UpdateChunkPosition() {
    }

    public UpdateChunkPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
