package org.sandboxpowered.silica.world.gen

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.api.block.Blocks
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube

class TerrainGenerator private constructor() {

    private val air by lazy { Blocks.AIR.get().baseState }
    private val bedrock by lazy { Blocks.BEDROCK.get().baseState }
    private val stone by lazy { Blocks.STONE.get().baseState }
    private val dirt by lazy { Blocks.DIRT.get().baseState }
    private val grass by lazy { Blocks.GRASS_BLOCK.get().baseState }

    fun fill(sx: Int, sy: Int, sz: Int, chunk: BlocTree) {
        if (sy > 0) return
        iterateCube(sx, sy, sz, w = 16, h = 7) { x, y, z ->
            if (x % 2 == z % 2) chunk[x, y, z] = when (y) {
                0 -> bedrock
                1, 2, 3 -> stone
                4, 5 -> dirt
                6 -> grass
                else -> air
            }
        }
        println("Generated $sx, $sy, $sz")
    }

    companion object {
        fun actor(): Behavior<Command> = Behaviors.setup {
            Actor(TerrainGenerator(), it)
        }
    }

    sealed class Command {
        data class Generate(val x: Int, val y: Int, val z: Int, val chunk: BlocTree, val replyTo: ActorRef<Generate>) : Command()
    }

    private class Actor(private val terrainGenerator: TerrainGenerator, context: ActorContext<Command>?) :
        AbstractBehavior<Command>(context) {

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleGenerate)
            .build()

        private fun handleGenerate(generate: Command.Generate): Behavior<Command> {
            val (x, y, z, chunk, replyTo) = generate
            terrainGenerator.fill(x,  y, z, chunk)
            replyTo.tell(generate)

            return Behaviors.same()
        }
    }
}