package org.sandboxpowered.silica.network;

public interface Packet extends PacketBase {

    void handle(PacketHandler packetHandler, Connection connection);
}
