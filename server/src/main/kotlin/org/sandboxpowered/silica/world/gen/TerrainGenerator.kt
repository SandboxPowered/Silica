package org.sandboxpowered.silica.world.gen

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.api.util.extensions.onMessage
import org.sandboxpowered.silica.api.util.math.ChunkPosition
import org.sandboxpowered.silica.content.block.Blocks
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

sealed interface TerrainGenerator {
    data class Generate(val x: Int, val y: Int, val z: Int, val chunk: BlocTree, val replyTo: ActorRef<in Generate>) :
        TerrainGenerator

    companion object {
        fun actor(): Behavior<TerrainGenerator> = Behaviors.setup {
            TerrainGeneratorActor(SimpleFiller(), it)
        }
    }
}

private class TerrainGeneratorActor(private val filler: ChunkFiller, context: ActorContext<TerrainGenerator>?) :
    AbstractBehavior<TerrainGenerator>(context) {

    override fun createReceive(): Receive<TerrainGenerator> = newReceiveBuilder()
        .onMessage(this::handleGenerate)
        .build()

    private fun handleGenerate(generate: TerrainGenerator.Generate): Behavior<TerrainGenerator> {
        val (x, y, z, chunk, replyTo) = generate
        val time = measureTimeMillis {
            filler.fill(x, y, z, chunk)
        }
        context.log.debug("Generated $x, $y, $z in $time millis")
        replyTo.tell(generate)

        return Behaviors.same()
    }
}

interface ChunkFiller {
    fun fill(sx: Int, sy: Int, sz: Int, chunk: BlocTree)
}

private class SimpleFiller : ChunkFiller {
    private fun Int.toChunk() = this shr ChunkPosition.TREES_DEPTH

    override fun fill(sx: Int, sy: Int, sz: Int, chunk: BlocTree) {
        if (sy > 0) return

        val pair = sx.toChunk().absoluteValue % 2 == sz.toChunk().absoluteValue % 2
        iterateCube(sx, sy, sz, w = 16, h = 7) { x, y, z ->
            chunk[x, y, z] = when (y) {
                0 -> Blocks.BEDROCK.defaultState
                1, 2, 3 -> Blocks.STONE.defaultState
                4, 5 -> Blocks.DIRT.defaultState
                6 -> (if (pair) Blocks.GRASS_BLOCK else Blocks.DIRT).defaultState
                else -> Blocks.AIR.defaultState
            }
        }
    }
}
