package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;

public class SetPlayerPositionAndLook implements PacketPlay {
    private double x, y, z;
    private float yaw, pitch;
    private byte flags;
    private int id;

    public SetPlayerPositionAndLook() {
    }

    public SetPlayerPositionAndLook(double x, double y, double z, float yaw, float pitch, byte flags, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.flags = flags;
        this.id = id;
    }

    @Override
    public void read(PacketByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        flags = buf.readByte();
        id = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeByte(flags);
        buf.writeVarInt(id);
        buf.writeBoolean(false); // dismount vehicle
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
