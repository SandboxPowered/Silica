package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.api.block.Blocks
import org.sandboxpowered.api.block.entity.BlockEntity
import org.sandboxpowered.api.entity.Entity
import org.sandboxpowered.api.item.ItemStack
import org.sandboxpowered.api.shape.Box
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.FluidState
import org.sandboxpowered.api.tags.TagManager
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.world.BlockFlag
import org.sandboxpowered.api.world.World
import org.sandboxpowered.api.world.WorldReader
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.util.BlocTree
import java.util.*
import java.util.stream.Stream

class SilicaWorld(private val side: Side) : World {
    private val blocks: BlocTree = BlocTree(WORLD_MIN, WORLD_MIN, WORLD_MIN, WORLD_SIZE, Blocks.AIR.get().baseState)
    private val artemisWorld: com.artemis.World? = null
    private var worldTicks = 0L

    override fun getBlockState(position: Position): BlockState {
        return blocks[position.x, position.y, position.z]
    }

    override fun setBlockState(position: Position, state: BlockState, vararg flags: BlockFlag): Boolean {
        blocks[position.x, position.y, position.z] = state
        return true
    }

    override fun getBlockEntity(position: Position): BlockEntity? {
        return null
    }

    override fun getFluidState(position: Position): FluidState {
        return getBlockState(position).fluidState
    }

    override fun getEntitiesWithin(box: Box): Stream<Entity> {
        return Stream.empty()
    }

    override fun <T : Entity?> getEntitiesWithin(box: Box, filter: Class<T>): Stream<T> {
        return Stream.empty()
    }

    override fun getWorldTime(): Long {
        return worldTicks
    }

    override fun getTagManager(): TagManager? {
        return null
    }

    override fun getSide(): Side = this.side

    override fun spawnItem(x: Double, y: Double, z: Double, stack: ItemStack) {}

    companion object {
        private const val WORLD_MIN = -1 shl 25 // -2^25
        private const val WORLD_MAX = (1 shl 25) - 1 // (2^25)-1
        private const val WORLD_SIZE = -WORLD_MIN + WORLD_MAX + 1 // 2^26

        fun actor(side: Side): Behavior<Command> = Behaviors.setup {
            SilicaWorldActor(SilicaWorld(side), it)
        }
    }

    sealed class Command {
        class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : Command() {
            class Tock(val done: ActorRef<Command>)
        }

        class Perform(val body: (World) -> Unit) : Command()
        class Ask<T>(val body: (WorldReader) -> T, val replyTo: ActorRef<T>) : Command()
    }

    private class SilicaWorldActor(private val world: SilicaWorld, context: ActorContext<Command>?) :
        AbstractBehavior<Command>(context) {

        private val commandQueue: Deque<Command> = LinkedList()

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleAsk)
            .onMessage(this::handleDelayedCommand)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<Command> {
            this.processCommandQueue()

            val w = world.artemisWorld
            if (w != null) {
                w.delta = tick.delta
                w.process()
            }
            ++world.worldTicks

            tick.replyTo.tell(Command.Tick.Tock(context.self))
            return Behaviors.same()
        }

        private fun processCommandQueue() {
            var next = commandQueue.pollFirst()
            while (next != null) {
                when (next) {
                    is Command.Perform -> next.body(world)
                    else -> error("Unhandled command in queue : $next")
                }
                next = commandQueue.pollFirst()
            }
        }

        private fun handleDelayedCommand(command: Command): Behavior<Command> {
            commandQueue += command
            return Behaviors.same()
        }

        private fun handleAsk(ask: Command.Ask<Any>): Behavior<Command> {
            ask.replyTo.tell(ask.body(world))
            return Behaviors.same()
        }
    }


}