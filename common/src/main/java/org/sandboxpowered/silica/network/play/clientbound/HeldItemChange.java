package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class HeldItemChange implements Packet {
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
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
