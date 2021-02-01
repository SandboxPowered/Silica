package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.*;

public class TeleportConfirmation implements PacketPlay {
    private int tpId;

    @Override
    public void read(PacketByteBuf buf) {
        tpId = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
