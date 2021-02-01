package org.sandboxpowered.silica.network;

public interface PacketBase {

    void read(PacketByteBuf buf);

    void write(PacketByteBuf buf);
}
