package org.sandboxpowered.silica.network.login.clientbound;

import org.sandboxpowered.silica.network.*;

public class Disconnect implements Packet {
    private String reason;

    public Disconnect(String reason) {
        this.reason = reason;
    }

    public Disconnect() {
    }

    @Override
    public void read(PacketByteBuf buf) {
        reason = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(reason);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
