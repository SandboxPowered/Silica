package org.sandboxpowered.silica.client.opengl;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.sandboxpowered.silica.world.util.BlocTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;

public class ChunkUtils {
    private static final byte EMPTY_VOXEL = 0;
    private static final int CHUNK_SIZE_SHIFT = 5; // 4 - 16, 5 - 32
    private static final int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;
    private static final int MAX_ACTIVE_CHUNKS = 65536;
    private static final int MAX_RENDER_DISTANCE_CHUNKS = 40;
    private static final int MAX_RENDER_DISTANCE_METERS = MAX_RENDER_DISTANCE_CHUNKS << CHUNK_SIZE_SHIFT;

    private static final Object2ObjectMap<Vector3i, RenderChunk> chunkByCoordinate = new Object2ObjectOpenHashMap<>();

    private static final List<RenderChunk> allChunks = new ArrayList<>();

    private static final List<RenderChunk> frontierChunks = new ArrayList<>();

    private static final ExecutorService executorService = Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() / 2), r -> {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("Chunk builder");
        t.setDaemon(true);
        return t;
    });

    private static final AtomicInteger chunkBuildTasksCount = new AtomicInteger();
    private static float nxX, nxY, nxZ, nxW, pxX, pxY, pxZ, pxW, nyX, nyY, nyZ, nyW, pyX, pyY, pyZ, pyW;
    private static Vector3d playerPosition;
    private static final Comparator<RenderChunk> inView = comparing(ChunkUtils::chunkNotInFrustum);
    private static final Comparator<RenderChunk> byDistance = comparingDouble(ChunkUtils::distToChunk);
    private static final Comparator<RenderChunk> inViewAndDistance = inView.thenComparing(byDistance);

    public static void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2000L, TimeUnit.MILLISECONDS))
                throw new AssertionError();
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    public static RenderChunk createChunk(BlocTree tree, int x, int y, int z) {
        RenderChunk chunk = new RenderChunk(x, y, z);
        allChunks.add(chunk);
        chunkByCoordinate.put(new Vector3i(x, y, z), chunk);
        addFreshChunk(chunk);
        BlocTree smallestNode = tree.get(x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE);


        chunkBuildTasksCount.incrementAndGet();
        return chunk;
    }

    private static int onChunkRemoved(RenderChunk chunk) {
        double d = distToChunk(chunk.cx, chunk.cy, chunk.cz);
        return onChunkRemoved(chunk.cx - 1, chunk.cy, chunk.cz, d)
                + onChunkRemoved(chunk.cx + 1, chunk.cy, chunk.cz, d)
                + onChunkRemoved(chunk.cx, chunk.cy, chunk.cz - 1, d)
                + onChunkRemoved(chunk.cx, chunk.cy, chunk.cz + 1, d)
                + onChunkRemoved(chunk.cx, chunk.cy - 1, chunk.cz, d)
                + onChunkRemoved(chunk.cx, chunk.cy + 1, chunk.cz, d);
    }

    private static boolean culledXY(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) < -nxW
                || pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) < -pxW
                || nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) < -nyW
                || pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) < -pyW;
    }

    private static boolean chunkNotInFrustum(RenderChunk chunk) {
        float xf = (chunk.cx << CHUNK_SIZE_SHIFT) - (float) floor(playerPosition.x);
        float yf = (chunk.cy << CHUNK_SIZE_SHIFT) - (float) floor(playerPosition.y);
        float zf = (chunk.cz << CHUNK_SIZE_SHIFT) - (float) floor(playerPosition.z);
        return culledXY(xf, yf, zf, xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE);
    }

    private static boolean chunkInRenderDistance(int x, int y, int z) {
        return distToChunk(x, y, z) < MAX_RENDER_DISTANCE_METERS * MAX_RENDER_DISTANCE_METERS;
    }

    private static int onChunkRemoved(int cx, int cy, int cz, double d) {
        RenderChunk n = chunkByCoordinate.get(new Vector3i(cx, cy, cz));
        if (n != null) {
            n.neighbors--;
            if (!frontierChunks.contains(n) && (chunkInRenderDistance(cx, cy, cz) || distToChunk(cx, cy, cz) < d)) {
                frontierChunks.add(n);
                return 1;
            }
        }
        return 0;
    }

    private static double distToChunk(RenderChunk chunk) {
        return distToChunk(chunk.cx, chunk.cy, chunk.cz);
    }

    private static double distToChunk(int cx, int cy, int cz) {
        double dx = playerPosition.x - ((cx + 0.5) * CHUNK_SIZE);
        double dy = playerPosition.y - ((cy + 0.5) * CHUNK_SIZE);
        double dz = playerPosition.z - ((cz + 0.5) * CHUNK_SIZE);
        return dx * dx + dy * dy + dz * dz;
    }

    private static void addFreshChunk(RenderChunk chunk) {
        frontierChunks.add(chunk);

        updateFrontierChunk(chunk, chunk.cx - 1, chunk.cy, chunk.cz);
        updateFrontierChunk(chunk, chunk.cx + 1, chunk.cy, chunk.cz);
        updateFrontierChunk(chunk, chunk.cx, chunk.cy, chunk.cz - 1);
        updateFrontierChunk(chunk, chunk.cx, chunk.cz, chunk.cz + 1);
        updateFrontierChunk(chunk, chunk.cx, chunk.cy - 1, chunk.cz);
        updateFrontierChunk(chunk, chunk.cx, chunk.cz + 1, chunk.cz);
    }

    private static void updateFrontierChunk(RenderChunk frontier, int cx, int cy, int cz) {
        RenderChunk n = chunkByCoordinate.get(new Vector3i(cx, cy, cz));
        if (n != null) {
            n.neighbors++;
            frontier.neighbors++;
            if (n.neighbors == 6) {
                frontierChunks.remove(n);
            }
        }
    }

    private boolean playerInsideChunk(RenderChunk chunk) {
        float margin = CHUNK_SIZE * 0.5f;
        int minX = chunk.cx << CHUNK_SIZE_SHIFT, maxX = minX + CHUNK_SIZE;
        int minZ = chunk.cz << CHUNK_SIZE_SHIFT, maxZ = minZ + CHUNK_SIZE;
        return playerPosition.x + margin >= minX && playerPosition.x - margin <= maxX && playerPosition.z + margin >= minZ && playerPosition.z - margin <= maxZ;
    }

    public static class RenderChunk {
        private final int cx;
        private final int cy;
        private final int cz;
        public int neighbors;
        private boolean built;

        public RenderChunk(int cx, int cy, int cz) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
        }
    }
}
