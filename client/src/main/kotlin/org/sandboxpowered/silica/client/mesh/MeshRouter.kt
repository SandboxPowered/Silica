package org.sandboxpowered.silica.client.mesh

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.javadsl.*
import org.sandboxpowered.silica.client.SilicaClient
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.api.world.WorldReader

/**
 * The MeshRouter is an Actor that is in charge of generating chunk meshes.
 * It is given an amount of [MeshWorker]s to command.
 * When it receives a request for a mesh, it sends it to a worker and awaits its response with a [RenderChunk].
 */
class MeshRouter(silica: SilicaClient, context: ActorContext<Command>) : AbstractBehavior<MeshRouter.Command>(context) {
    companion object {
        fun actor(client: SilicaClient): Behavior<Command> = Behaviors.setup { MeshRouter(client, it) }
    }

    val poolRef: ActorRef<MeshWorker.Command>

    init {
        val poolSize = 1.coerceAtLeast(Runtime.getRuntime().availableProcessors() / 2)
        val poolRouter = Routers.pool(
            poolSize,
            Behaviors.supervise(MeshWorker.actor(silica.modelLoader, context.self))
                .onFailure(SupervisorStrategy.restart())
        )
        poolRef = context.spawn(poolRouter, "worker-pool")
    }

    sealed class Command {
        class MeshConstructed(val chunk: RenderChunk) : Command()
        class RequestConstruction(val chunkPos: ChunkPos, val world: WorldReader) : Command()
    }

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(this::onRequest)
            .onMessage(this::onConstructed)
            .build()
    }

    private fun onConstructed(msg: Command.MeshConstructed): Behavior<Command> {
        println("Built ${msg.chunk.pos}")
        return Behaviors.same()
    }

    private fun onRequest(msg: Command.RequestConstruction): Behavior<Command> {
        poolRef.tell(MeshWorker.Command.ConstructMesh(msg.chunkPos, msg.world))
        return Behaviors.same()
    }
}