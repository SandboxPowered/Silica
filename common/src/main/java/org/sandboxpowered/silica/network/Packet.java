package org.sandboxpowered.silica.network;

public interface Packet {
    void read(PacketByteBuf buf);

    void write(PacketByteBuf buf);

    void handle(PacketHandler packetHandler, Connection connection);
}
