package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.*;

public class PlayerMovement implements PacketPlay {
    private boolean onGround;

    public PlayerMovement() {
    }

    public PlayerMovement(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public void read(PacketByteBuf buf) {
        onGround = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(onGround);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
