package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class HeldItemChange implements PacketPlay {
    private byte slot;

    public HeldItemChange() {
    }

    public HeldItemChange(byte slot) {
        this.slot = slot;
    }

    @Override
    public void read(PacketByteBuf buf) {
        this.slot = buf.readByte();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(slot);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
