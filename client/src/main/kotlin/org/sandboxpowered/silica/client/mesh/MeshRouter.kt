package org.sandboxpowered.silica.client.mesh

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.javadsl.*
import org.sandboxpowered.silica.util.extensions.onMessage

/**
 * The MeshRouter is an Actor that is in charge of generating chunk meshes.
 * It is given an amount of [MeshWorker]s to command.
 * When it receives a request for a mesh, it sends it to a worker and awaits its response with a [RenderChunk].
 */
class MeshRouter(context: ActorContext<Command>) : AbstractBehavior<MeshRouter.Command>(context) {
    val poolRef: ActorRef<MeshWorker.Command>

    init {
        val poolSize = 1.coerceAtLeast(Runtime.getRuntime().availableProcessors() / 2)
        val poolRouter = Routers.pool(
            poolSize, Behaviors.supervise(MeshWorker.actor(context.self))
                .onFailure(SupervisorStrategy.restart())
        )
        poolRef = context.spawn(poolRouter, "worker-pool")
    }

    sealed class Command {
        class MeshConstructed(val chunk: RenderChunk) : Command()
    }

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(this::onMessage)
            .build()
    }

    private fun onMessage(msg: Command.MeshConstructed): Behavior<Command> {
        println("Built ${msg.chunk.pos}")
        return Behaviors.same()
    }
}