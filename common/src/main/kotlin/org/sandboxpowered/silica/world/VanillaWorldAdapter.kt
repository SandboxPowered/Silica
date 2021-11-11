package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.mostlyoriginal.api.event.common.Subscribe
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.ecs.events.ReplaceBlockEvent
import org.sandboxpowered.silica.util.extensions.WithContext
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.network.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.world.util.BlocTree
import kotlin.math.floor

sealed class VanillaWorldAdapter {
    class GetChunkSection(
        val pos: ChunkSectionPos,
        val replyTo: ActorRef<in VanillaChunkSection>
    ) : VanillaWorldAdapter()

    class SetChunkSection(
        val pos: ChunkSectionPos,
        val chunk: BlocTree
    ) : VanillaWorldAdapter()

    class Update(val event: ReplaceBlockEvent) : VanillaWorldAdapter()

    companion object {
        fun actor(world: ActorRef<in SilicaWorld.Command>, mapper: StateMappingManager): Behavior<VanillaWorldAdapter> =
            Behaviors.setup { VanillaWorldAdapterActor(world, mapper, it) }
    }
}

private class VanillaWorldAdapterActor(
    private val world: ActorRef<in SilicaWorld.Command>,
    private val mapper: StateMappingManager,
    context: ActorContext<VanillaWorldAdapter>?
) : AbstractBehavior<VanillaWorldAdapter>(context), WithContext {

    init {
        world.tell(SilicaWorld.Command.RegisterEventSubscriber(this))
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
                world.ask { ref: ActorRef<BlocTree> -> SilicaWorld.Command.DelayedCommand.AskSilica(ref) { it.getTerrain() } }
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
        val chunkPos = message.event.pos.toChunkSection
        vanillaChunkSectionMap[chunkPos]?.set(message.event.pos, message.event.oldState, message.event.newState)

        return Behaviors.same()
    }

    @Subscribe
    fun onUpdate(event: ReplaceBlockEvent) = context.self.tell(VanillaWorldAdapter.Update(event))
}

data class ChunkSectionPos(val x: Int, val y: Int, val z: Int)

private val Position.toChunkSection: ChunkSectionPos
    get() {
        return ChunkSectionPos(floor(x / 16f).toInt(), floor(y / 16f).toInt(), floor(z / 16f).toInt())
    }
