package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;

public class ClientSettings implements PacketPlay {
    private String language;
    private byte renderDistance;
    private int chatMode;
    private boolean enableColour;
    private short displayedSkin;
    private int hand;

    @Override
    public void read(PacketByteBuf buf) {
        language = buf.readString(16);
        renderDistance = buf.readByte();
        chatMode = buf.readVarInt();
        enableColour = buf.readBoolean();
        displayedSkin = buf.readUnsignedByte();
        hand = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {
    }
}