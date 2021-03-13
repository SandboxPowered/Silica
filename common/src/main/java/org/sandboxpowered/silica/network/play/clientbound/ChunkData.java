package org.sandboxpowered.silica.network.play.clientbound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kotlin.jvm.functions.Function1;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.silica.nbt.CompoundTag;
import org.sandboxpowered.silica.network.*;
import org.sandboxpowered.silica.network.play.clientbound.world.VanillaChunkSection;
import org.sandboxpowered.silica.network.util.BitPackedLongArray;
import org.sandboxpowered.silica.world.util.BlocTree;

public class ChunkData implements PacketPlay {
    private int cX, cZ;
    private int bitMask;
    private byte[] buffer;
    private int[] biomes;
    private final VanillaChunkSection[] sections = new VanillaChunkSection[16];

    public ChunkData() {
    }

    public ChunkData(int cX, int cZ, BlocTree blocTree, Function1<BlockState, Integer> stateToId) {
        this.cX = cX;
        this.cZ = cZ;

        this.biomes = new int[1024];

        for (int i = 0; i < 16; ++i) {
            sections[i] = new VanillaChunkSection(blocTree, cX * 16, i * 16, cZ * 16, stateToId);
        }
        this.buffer = new byte[calculateSize(cX, cZ)];
        bitMask = extractData(new PacketByteBuf(getWriteBuffer()), cX, cZ, blocTree);
    }

    private int extractData(PacketByteBuf packetByteBuf, int cX, int cZ, BlocTree blocTree) {
        int j = 0;
        for (int i = 0; i < 16; ++i) {
            sections[i].write(packetByteBuf);
            j |= 1 << i; // TODO: only write non-empty
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

    private int calculateSize(int cX, int cZ) {
        int r = 0;
        for (int i = 0; i < 16; i++) {
            r += sections[i].getSerializedSize();
        }
        return r;
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(cX);
        buf.writeInt(cZ);
        buf.writeBoolean(true);
        buf.writeVarInt(bitMask);
        final CompoundTag heightmap = new CompoundTag();
        final BitPackedLongArray heightmapData = new BitPackedLongArray(256, 9);
        for (int i = 0; i < 256; i++) heightmapData.set(i, 7);
        heightmap.setLongArray("MOTION_BLOCKING", heightmapData.getData());
        heightmap.setLongArray("WORLD_SURFACE", heightmapData.getData());
        buf.writeNBT(heightmap);
        buf.writeVarIntArray(biomes);
        buf.writeVarInt(buffer.length);
        buf.writeBytes(buffer);
        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
