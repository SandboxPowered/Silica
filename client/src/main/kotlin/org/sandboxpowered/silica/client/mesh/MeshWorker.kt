package org.sandboxpowered.silica.client.mesh

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.unimi.dsi.fastutil.floats.FloatList
import org.sandboxpowered.silica.client.model.ModelLoader
import org.sandboxpowered.silica.client.util.ChunkUtils
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.world.WorldReader
import org.sandboxpowered.silica.world.util.iterateCube
import kotlin.random.Random

class MeshWorker(
    val modelLoader: ModelLoader,
    val router: ActorRef<MeshRouter.Command>,
    context: ActorContext<Command>
) :
    AbstractBehavior<MeshWorker.Command>(context) {
    sealed class Command {
        class ConstructMesh(val chunkPos: ChunkPos, val world: WorldReader) : Command()
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::onMessage)
        .build()

    private fun onMessage(msg: Command.ConstructMesh): Behavior<Command> {
        val worldPos = msg.chunkPos.toWorldPos()
        val vertexData: FloatList = FloatList.of()
        iterateCube(0, 0, 0, ChunkUtils.CHUNK_SIZE) { x, y, z ->
            val combinedPosition = (x shl 8) or (y shl 4) or z
            val globalPos = worldPos.add(x, y, z)
            val state = msg.world.getBlockState(globalPos)

            if (!state.isAir) {
                val model = modelLoader.getModel(state)

                for (dir in Direction.ALL) {
                    val offset = globalPos.shift(dir)
                    val offsetState = msg.world.getBlockState(offset)
                    if (!offsetState.isAir) continue
                    val quads = model.getQuads(state, dir, Random.Default)

                    quads.forEach { quad ->
                        vertexData.addAll(quad.vertexData)
                    }
                }
            }
        }

        val chunk = RenderChunk(msg.chunkPos, vertexData.toFloatArray())

        router.tell(MeshRouter.Command.MeshConstructed(chunk))
        return Behaviors.same()
    }

    companion object {
        fun actor(loader: ModelLoader, router: ActorRef<MeshRouter.Command>): Behavior<Command> =
            Behaviors.setup { MeshWorker(loader, router, it) }
    }
}

private fun FloatList.addAll(vertexData: FloatArray) {
    size(size + vertexData.size)
    addAll(vertexData.asSequence())
}
