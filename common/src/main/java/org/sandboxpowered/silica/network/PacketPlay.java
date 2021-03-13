package org.sandboxpowered.silica.network;

public interface PacketPlay extends PacketBase {

    void handle(PacketHandler packetHandler, PlayContext context);
}
