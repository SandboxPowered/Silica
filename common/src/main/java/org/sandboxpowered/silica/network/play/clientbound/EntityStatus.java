package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class EntityStatus implements PacketPlay {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(0);
        buf.writeByte(24);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
