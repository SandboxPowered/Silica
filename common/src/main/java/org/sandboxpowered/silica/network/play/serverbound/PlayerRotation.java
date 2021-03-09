package org.sandboxpowered.silica.network.play.serverbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;

public class PlayerRotation implements PacketPlay {
    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerRotation() {
    }

    public PlayerRotation(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void read(PacketByteBuf buf) {
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        onGround = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}
