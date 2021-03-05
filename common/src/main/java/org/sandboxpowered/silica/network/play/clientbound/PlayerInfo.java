package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;

import java.util.UUID;

public class PlayerInfo implements PacketPlay {
    private int action;

    public PlayerInfo() {
    }

    public PlayerInfo(int action) {
        this.action = action;
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(action);
        buf.writeVarInt(1);

        buf.writeUUID(UUID.fromString("221141c3-340d-4c3b-8b36-6351b6ea6182"));
        switch (action) {
            case 0 -> {
                buf.writeString("The_CodedOne", 16);
                buf.writeVarInt(0);
                buf.writeVarInt(1);
                buf.writeVarInt(1);
                buf.writeBoolean(false);
            }
            case 2 -> {
                buf.writeVarInt(1);
            }
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
