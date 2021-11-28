package org.sandboxpowered.silica.world.persistence

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.stream.javadsl.*
import akka.util.ByteString
import akka.util.ByteStringBuilder
import org.sandboxpowered.silica.api.util.extensions.WithContext
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.extensions.onMessage
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.world.util.BlocTree
import scala.util.Either
import scala.util.Left
import scala.util.Right
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*

sealed interface WorldStorage {

    /**
     * World coords, not chunk coords !
     */
    class Load(val x: Int, val y: Int, val z: Int, val replyTo: ActorRef<in Either<BlocTree, Unit>>) : WorldStorage

    class Persist(val blocks: BlocTree, val replyTo: ActorRef<in Boolean>) : WorldStorage

    companion object {
        fun actor(mapper: BlockStateMapping): Behavior<WorldStorage> = Behaviors.setup {
            WorldStorageActor(mapper, it)
        }
    }
}

private class WorldStorageActor(
    private val mapper: BlockStateMapping,
    context: ActorContext<WorldStorage>
) : AbstractBehavior<WorldStorage>(context), WithContext<WorldStorage> {

    override fun createReceive(): Receive<WorldStorage> = newReceiveBuilder()
        .onMessage(this::handleLoad)
        .onMessage(this::handlePersist)
        .build()

    private fun handleLoad(load: WorldStorage.Load): Behavior<WorldStorage> {
        val logger = logger
        val folder = Path("world")
        if (folder.isDirectory()) {
            val path = folder.resolve(load.fileName())
            if (path.isRegularFile()) {
                FileIO.fromPath(path)
                    .via(Framing.delimiter(ByteString.fromString(MAGIC), 16384, FramingTruncation.DISALLOW))
                    .filter(ByteString::nonEmpty)
                    .map(ByteString::iterator)
                    .map {
                        val result = LinkedList<Either<BlockState, Unit>>()
                        val size = it.getInt(ByteOrder.LITTLE_ENDIAN)
                        val default = mapper[it.getInt(ByteOrder.LITTLE_ENDIAN)]
                        while (it.nonEmpty()) when (val read = it.getInt(ByteOrder.LITTLE_ENDIAN)) {
                            -1 -> result += Right(Unit)
                            else -> result += Left(mapper[read])
                        }
                        Left<BlocTree, Unit>(BlocTree().apply {
                            init(0, load.x, load.y, load.z, size, default)
                            read(result)
                        })
                    }.runForeach(load.replyTo::tell, context.system)
                    .onException {
                        logger.warn("Couldn't load ${load.fileName()}", it)
                        load.replyTo.tell(Right(Unit))
                    }
                return Behaviors.same()
            }
        }

        load.replyTo.tell(Right(Unit))
        return Behaviors.same()
    }

    private fun handlePersist(persist: WorldStorage.Persist): Behavior<WorldStorage> {
        Path("world").takeUnless { it.exists() }?.createDirectory()
        Source.lazyCompletionStage {
            CompletableFuture.completedFuture(persist.blocks.bytes())
        }.toMat(
            FileIO.toPath(Path("world", persist.blocks.fileName())),
            Keep.right()
        ).run(context.system).thenAccept {
            logger.info("Saved ${persist.blocks.fileName()}")
            persist.replyTo.tell(true)
        }.onException {
            logger.warn("Unable to save ${persist.blocks.fileName()}", it)
            persist.replyTo.tell(false)
        }

        return Behaviors.same()
    }

    private fun WorldStorage.Load.fileName() = "chunk_${x}_${y}_${z}.si"
    private fun BlocTree.fileName() = "chunk_${selection.x}_${selection.y}_${selection.z}.si"

    private fun BlocTree.bytes(): ByteString {
        val builder = ByteStringBuilder().addAll(ByteString.fromString(MAGIC))
        builder.putInt(selection.width, ByteOrder.LITTLE_ENDIAN)
        builder.putInt(mapper[default], ByteOrder.LITTLE_ENDIAN)
        return serialize().fold(
            builder
        ) { acc, it ->
            acc.putInt(
                when (it) {
                    is Left -> mapper[it.value()]
                    else -> -1
                }, ByteOrder.LITTLE_ENDIAN
            )
        }.addAll(ByteString.fromString(MAGIC)).result()
    }

    private companion object {
        private const val MAGIC = "silica"
    }
}