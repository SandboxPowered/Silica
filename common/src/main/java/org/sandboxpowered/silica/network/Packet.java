package org.sandboxpowered.silica.network;

import org.sandboxpowered.silica.server.SilicaServer;

public interface Packet {
    void read(PacketByteBuf buf);

    void write(PacketByteBuf buf);

    void handle(PacketHandler packetHandler, SilicaServer server);
}
