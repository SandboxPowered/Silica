package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class DeclareRecipes implements PacketPlay {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
