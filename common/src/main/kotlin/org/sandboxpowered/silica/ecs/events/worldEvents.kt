package org.sandboxpowered.silica.ecs.events

import net.mostlyoriginal.api.event.common.Event
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.world.state.block.BlockState

data class ReplaceBlockEvent(
    val pos: Position,
    val oldState: BlockState,
    val newState: BlockState
) : Event