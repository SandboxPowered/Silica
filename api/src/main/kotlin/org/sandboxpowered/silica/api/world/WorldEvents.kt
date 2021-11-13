package org.sandboxpowered.silica.api.world

import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.WorldEvents.ReplaceBlocksEvent
import org.sandboxpowered.silica.api.world.state.block.BlockState

object WorldEvents {
    val REPLACE_BLOCKS_EVENT: Event<ReplaceBlocksEvent> = EventFactory.createEvent { handlers ->
        ReplaceBlocksEvent { pos, old, new -> handlers.forEach { it(pos, old, new) } }
    }

    fun interface ReplaceBlocksEvent {
        operator fun invoke(pos: Position, old: BlockState, new: BlockState)
    }
}