package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class DeclareCommands implements PacketPlay {
    public DeclareCommands() {
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(1);

        buf.writeByte(0);
        buf.writeVarInt(0);

        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
