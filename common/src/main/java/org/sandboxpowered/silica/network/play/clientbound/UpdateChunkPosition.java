package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;

public class UpdateChunkPosition implements PacketPlay {
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
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
