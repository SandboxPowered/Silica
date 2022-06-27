package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.sandboxpowered.silica.api.util.extensions.WithContext
import org.sandboxpowered.silica.api.util.extensions.onMessage
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldEvents
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.vanilla.network.util.mapping.BlockStateProtocolMapping
import kotlin.math.floor

sealed class VanillaWorldAdapter {
    class GetChunkSection(
        val pos: ChunkSectionPos,
        val replyTo: ActorRef<in VanillaChunkSection>
    ) : VanillaWorldAdapter()

    class SetChunkSection(val pos: ChunkSectionPos, val chunk: WorldReader) : VanillaWorldAdapter()

    class Update(val pos: Position, val old: BlockState, val new: BlockState, val flag: WorldWriter.Flag) :
        VanillaWorldAdapter()

    companion object {
        fun actor(world: ActorRef<in World.Command>, mapper: BlockStateProtocolMapping): Behavior<VanillaWorldAdapter> =
            Behaviors.setup { VanillaWorldAdapterActor(world, mapper, it) }
    }
}

private class VanillaWorldAdapterActor(
    private val world: ActorRef<in World.Command>,
    private val mapper: BlockStateProtocolMapping,
    context: ActorContext<VanillaWorldAdapter>
) : AbstractBehavior<VanillaWorldAdapter>(context), WithContext<VanillaWorldAdapter> {

    init {
        WorldEvents.REPLACE_BLOCKS_EVENT.subscribe(this::onUpdate)
    }

    override fun createReceive(): Receive<VanillaWorldAdapter> = newReceiveBuilder()
        .onMessage(this::setChunkSection)
        .onMessage(this::getChunkSection)
        .onMessage(this::onUpdate)
        .build()

    private val vanillaChunkSectionMap: MutableMap<ChunkSectionPos, VanillaChunkSection> = Object2ObjectOpenHashMap()

    private val onGoingQueries: MutableMap<ChunkSectionPos, MutableList<ActorRef<in VanillaChunkSection>>> =
        Object2ObjectOpenHashMap()

    private fun getChunkSection(message: VanillaWorldAdapter.GetChunkSection): Behavior<VanillaWorldAdapter> {
        val chunk = vanillaChunkSectionMap[message.pos]
        if (chunk == null) {
            val waiters = onGoingQueries[message.pos]
            if (waiters != null) waiters += message.replyTo
            else {
                onGoingQueries[message.pos] = mutableListOf(message.replyTo)
                world.ask { ref: ActorRef<WorldReader> -> World.Command.Ask(ref) { it } }
                    .thenAccept { context.self.tell(VanillaWorldAdapter.SetChunkSection(message.pos, it)) }
            }
        } else message.replyTo.tell(chunk)

        return Behaviors.same()
    }

    private fun setChunkSection(message: VanillaWorldAdapter.SetChunkSection): Behavior<VanillaWorldAdapter> {
        val (x, y, z) = message.pos
        val section = VanillaChunkSection(message.chunk, x * 16, y * 16, z * 16, this::remapToVanillaProtocol)
        vanillaChunkSectionMap[message.pos] = section

        onGoingQueries.remove(message.pos)?.forEach { it.tell(section) }

        return Behaviors.same()
    }

    private fun remapToVanillaProtocol(state: BlockState): Int = mapper[state]

    private fun onUpdate(message: VanillaWorldAdapter.Update): Behavior<VanillaWorldAdapter> {
        val chunkPos = message.pos.toChunkSection
        vanillaChunkSectionMap[chunkPos]?.set(message.pos, message.old, message.new)

        return Behaviors.same()
    }

    private fun onUpdate(pos: Position, old: BlockState, new: BlockState, flag: WorldWriter.Flag) =
        context.self.tell(VanillaWorldAdapter.Update(pos, old, new, flag))
}

data class ChunkSectionPos(val x: Int, val y: Int, val z: Int)

private val Position.toChunkSection: ChunkSectionPos
    get() {
        return ChunkSectionPos(floor(x / 16f).toInt(), floor(y / 16f).toInt(), floor(z / 16f).toInt())
    }
