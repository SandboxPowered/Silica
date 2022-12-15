package org.sandboxpowered.silica.world.util

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import org.sandboxpowered.silica.api.util.extensions.WithScheduler
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.ChunkPosition
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
    selection: WorldSelection,
    default: BlockState,
    override val scheduler: Scheduler,
    private val persistence: ActorRef<in WorldStorage>,
    private val generator: ActorRef<in TerrainGenerator.Generate>
) : BlocTreeAggregate(selection, default), WithScheduler {
    private val logger = getLogger()
    private val loading = mutableSetOf<ChunkKey>()

    private fun Int.alignToChunk() = floor(this / ChunkPosition.CHUNK_SIZE_F).toInt() * ChunkPosition.CHUNK_SIZE

    // TODO: ideally this would be in an actor for proper concurrency handling
    fun load(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            if (this.contains(x, y, z)) {
                val ax = x.alignToChunk()
                val ay = y.alignToChunk()
                val az = z.alignToChunk()
                val key = ChunkKey(ax, ay, az)
                if (key !in storage) {
                    // To make sure storage is grown in the correct thread
                    storage[key] = placeHolder
                    loading += key

                    persistence.ask(Duration.ofSeconds(5)) { it: ActorRef<in Either<BlocTree, Unit>> ->
                        WorldStorage.Load(ax, ay, az, it)
                    }.thenCompose {
                        when (it) {
                            is Right -> generator.ask(Duration.ofSeconds(5)) { ref: ActorRef<in TerrainGenerator.Generate> ->
                                TerrainGenerator.Generate(
                                    ax,
                                    ay,
                                    az,
                                    pooled(ax, ay, az, ChunkPosition.CHUNK_SIZE, default),
                                    ref
                                )
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
            val key = ChunkKey(x, y, z)
            if (this.contains(x, y, z) && key !in loading) {
                storage.remove(ChunkKey(x, y, z))?.let { chunk ->
                    persistence.ask { ref: ActorRef<in Boolean> -> WorldStorage.Persist(chunk, ref) }
                        .thenAccept { chunk.free() }
                }
            }
        }
    }

    fun persist() {
        storage.forEach { (_, chunk) ->
            persistence.ask { ref: ActorRef<in Boolean> -> WorldStorage.Persist(chunk, ref) }
        }
    }

    override fun contains(key: ChunkKey) = key !in loading && super.contains(key)

    private fun pooled(
        x: Int, y: Int, z: Int,
        size: Int, default: BlockState
    ): BlocTree = otPool.obtain().init(0, x, y, z, size, default)

    private val otPool: ObjectPool<BlocTree> get() = getPool()

    private fun BlocTree.free() = otPool.free(this)
}
