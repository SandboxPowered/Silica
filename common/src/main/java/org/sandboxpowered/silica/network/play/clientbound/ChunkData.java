package org.sandboxpowered.silica.network.play.clientbound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.play.clientbound.world.VanillaChunkSection;
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
    }

    private int extractData(PacketByteBuf packetByteBuf, int cX, int cZ, BlocTree blocTree) {
        int j = 0;
        for (int k = 0; k < 16; ++k) {
            new VanillaChunkSection(blocTree, cX * 16, k * 16, cZ * 16).write(packetByteBuf);
            j |= 1 << k; // TODO: only write non-empty
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
