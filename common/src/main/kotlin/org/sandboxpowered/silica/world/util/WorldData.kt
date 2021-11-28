package org.sandboxpowered.silica.world.util

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import org.sandboxpowered.silica.api.util.extensions.WithScheduler
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.WorldSelection
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.walk
import org.sandboxpowered.silica.world.gen.TerrainGenerator
import org.sandboxpowered.silica.world.persistence.WorldStorage
import scala.util.Either
import scala.util.Left
import scala.util.Right
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import kotlin.math.floor

class WorldData(
    override val selection: WorldSelection,
    private val default: BlockState,
    override val scheduler: Scheduler,
    private val persistence: ActorRef<in WorldStorage>,
    private val generator: ActorRef<in TerrainGenerator.Generate>
) : WorldSection, WithScheduler {
    private val logger = getLogger()

    private val placeHolder =
        BlocTree().init(0, Int.MIN_VALUE / 2, Int.MIN_VALUE / 2, Int.MIN_VALUE / 2, Int.MAX_VALUE, default)
    private val storage: Object2ObjectMap<Key, BlocTree> = Object2ObjectOpenHashMap()
    private val loading = mutableSetOf<Key>()

    private fun Int.alignToChunk() = floor(this / CHUNK_SIZE_F).toInt() * CHUNK_SIZE

    // TODO: ideally this would be in an actor for proper concurrency handling
    fun load(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            if (this.contains(x, y, z)) {
                val ax = x.alignToChunk()
                val ay = y.alignToChunk()
                val az = z.alignToChunk()
                val key = Key(ax, ay, az)
                if (key !in storage) {
                    // To make sure storage is grown in the correct thread
                    storage[key] = placeHolder
                    loading += key

                    persistence.ask(Duration.ofSeconds(5)) { it: ActorRef<in Either<BlocTree, Unit>> ->
                        WorldStorage.Load(ax, ay, az, it)
                    }.thenCompose {
                        when (it) {
                            is Right -> generator.ask(Duration.ofSeconds(5)) { ref: ActorRef<in TerrainGenerator.Generate> ->
                                TerrainGenerator.Generate(ax, ay, az, pooled(ax, ay, az, CHUNK_SIZE, default), ref)
                            }.thenApply(TerrainGenerator.Generate::chunk)
                            is Left -> CompletableFuture.completedFuture(it.value())
                            else -> error("Impossible")
                        }
                    }.thenAccept {
                        storage[key] = it
                        // TODO : check concurrent modif :s
                        loading -= key
                    }.onException {
                        // TODO : check concurrent modif :s
                        loading -= key
                        val message = "Unable to load chunk at $ax, $ay, $az in time"
                        when (it.cause) {
                            is TimeoutException -> logger.warn(message)
                            else -> logger.warn(message, it)
                        }
                    }
                }
            }
        }
    }

    fun unload(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            val key = Key(x, y, z)
            if (this.contains(x, y, z) && key !in loading) {
                storage.remove(Key(x, y, z))?.free()
            }
        }
    }

    fun persist() {
        storage.forEach { (_, chunk) ->
            persistence.ask { ref: ActorRef<in Boolean> -> WorldStorage.Persist(chunk, ref) }
        }
    }

    override fun set(x: Int, y: Int, z: Int, state: BlockState) {
        val key = Key(x, y, z)
        if (key !in loading) storage[key]?.set(x, y, z, state)
    }

    override fun get(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int): WorldSection {
        if (width != CHUNK_SIZE || height != CHUNK_SIZE || depth != CHUNK_SIZE) TODO("Non-chunk sections not implemented yet")
        // TODO : currently this just picks the chunk containing given coord
        return storage[Key(x, y, z)] ?: placeHolder
    }

    override fun get(x: Int, y: Int, z: Int): BlockState {
        val key = Key(x, y, z)
        return if (key !in loading) storage[key]?.get(x, y, z) ?: default else default
    }

    override fun nonAirInSection(x: Int, y: Int, z: Int): Int {
        val key = Key(x, y, z)
        return if (key !in loading) storage[Key(x, y, z)]?.nonAirInSection(x, y, z) ?: 0 else 0
    }

    private class Key(x: Int, y: Int, z: Int) {
        private val coarseX = x.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()
        private val coarseY = y.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()
        private val coarseZ = z.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Key) return false

            if (coarseX != other.coarseX) return false
            if (coarseY != other.coarseY) return false
            if (coarseZ != other.coarseZ) return false

            return true
        }

        override fun hashCode(): Int {
            return ((coarseX * 73856093u) xor (coarseY * 19349663u) xor (coarseZ * 83492791u)).toInt()
        }
    }

    private fun pooled(
        x: Int, y: Int, z: Int,
        size: Int, default: BlockState
    ): BlocTree = otPool.obtain().init(0, x, y, z, size, default)

    private val otPool: ObjectPool<BlocTree> get() = getPool()

    private fun BlocTree.free() = otPool.free(this)

    private companion object {
        private const val TREES_DEPTH = 4
        private const val CHUNK_SIZE = 1 shl TREES_DEPTH
        private const val CHUNK_SIZE_F = CHUNK_SIZE.toFloat()
    }
}
