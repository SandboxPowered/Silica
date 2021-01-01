package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.Protocol;

public class HandshakeRequest implements Packet {
    private int protocolVersion;
    private String hostName;
    private int port;
    private int intention;

    @Override
    public void read(PacketByteBuf buf) {
        protocolVersion = buf.readVarInt();
        hostName = buf.readString(255);
        port = buf.readUnsignedShort();
        intention = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.protocolVersion);
        buf.writeString(this.hostName);
        buf.writeShort(this.port);
        buf.writeVarInt(this.intention);
    }

    @Override
    public void handle(PacketHandler packetHandler) {
        Protocol protocol = Protocol.getProtocolFromId(this.intention);
        if (protocol != null) {
            packetHandler.setProtocol(protocol);
        }
    }
}
