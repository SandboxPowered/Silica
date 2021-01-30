package org.sandboxpowered.silica.network.play.clientbound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.world.util.BlocTree;

public class ChunkData implements Packet {
    private int cX, cZ;
    private int bitMask;
    private byte[] buffer;
    private int[] biomes;

    public ChunkData() {
    }

    public ChunkData(int cX, int cZ, BlocTree blocTree) {
        this.cX = cX;
        this.cZ = cZ;
        int k = 0;

        this.biomes = new int[1024];

        this.buffer = new byte[calculateSize(cX, cZ, blocTree)];
        bitMask = extractData(new PacketByteBuf(getWriteBuffer()), cX, cZ, blocTree);

        for (int l = 16; k < l; ++k) {
            bitMask |= 1 << k;
        }
    }

    private int extractData(PacketByteBuf packetByteBuf, int cX, int cZ, BlocTree blocTree) {
        int j = 0;
        int k = 0;
        for (int l = 16; k < l; ++k) {
            packetByteBuf.writeShort(0);
            packetByteBuf.writeByte(4);
            packetByteBuf.writeVarInt(1);
            packetByteBuf.writeVarIntArray(new int[]{0});
            j |= 1 << k;
        }
        return j;
    }

    public PacketByteBuf getReadBuffer() {
        return new PacketByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
        byteBuf.writerIndex(0);
        return byteBuf;
    }


    private int calculateSize(int cX, int cZ, BlocTree blocTree) {
        return 0;
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(cX);
        buf.writeVarInt(cZ);
        buf.writeBoolean(true);
        buf.writeVarInt(bitMask);
        // hightmap
        buf.writeVarIntArray(biomes);
        buf.writeVarInt(buffer.length);
        buf.writeBytes(buffer);
        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
