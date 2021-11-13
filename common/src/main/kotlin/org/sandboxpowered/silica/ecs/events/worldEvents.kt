package org.sandboxpowered.silica.ecs.events

import net.mostlyoriginal.api.event.common.Event
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.state.block.BlockState

@Deprecated("Use WorldEvents.REMOVE_ENTITIES_EVENT instead.") // Kept just in case
data class ReplaceBlockEvent(
    val pos: Position,
    val oldState: BlockState,
    val newState: BlockState
) : Event