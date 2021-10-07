package org.sandboxpowered.silica.client.mesh

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube

class MeshWorker(val router: ActorRef<MeshRouter.Command>, context: ActorContext<Command>) :
    AbstractBehavior<MeshWorker.Command>(context) {
    sealed class Command {
        class ConstructMesh(val chunkPos: ChunkPos, val blocTree: BlocTree)
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::onMessage)
        .build()

    private fun onMessage(msg: Command.ConstructMesh): Behavior<Command> {
        (15 shl 8) or (15 shl 4) or (15)

        iterateCube(0, 0, 0, 16, 16, 16) { x, y, z ->
            val combinedPosition = (x shl 8) or (y shl 4) or z
        }

        router.tell(MeshRouter.Command.MeshConstructed(RenderChunk(msg.chunkPos)))
        return Behaviors.same()
    }

    companion object {
        fun actor(router: ActorRef<MeshRouter.Command>): Behavior<Command> = Behaviors.setup { MeshWorker(router, it) }
    }
}