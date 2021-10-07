package org.sandboxpowered.silica.client.mesh

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.util.extensions.onMessage

class MeshWorker(val router: ActorRef<MeshRouter.Command>, context: ActorContext<Command>) :
    AbstractBehavior<MeshWorker.Command>(context) {
    sealed class Command {
        class ConstructMesh(val chunkPos: ChunkPos)
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::onMessage)
        .build()

    private fun onMessage(msg: Command.ConstructMesh): Behavior<Command> {
        router.tell(MeshRouter.Command.MeshConstructed(RenderChunk(msg.chunkPos)))
        return Behaviors.same()
    }

    companion object {
        fun actor(router: ActorRef<MeshRouter.Command>): Behavior<Command> = Behaviors.setup { MeshWorker(router, it) }
    }
}