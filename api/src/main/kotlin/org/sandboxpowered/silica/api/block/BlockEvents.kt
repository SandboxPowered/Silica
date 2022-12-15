package org.sandboxpowered.silica.api.block

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.silica.api.block.BlockEvents.InitializeArchetypeEvent
import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory

object BlockEvents {
    val INITIALIZE_ARCHETYPE_EVENT: Event<InitializeArchetypeEvent> = EventFactory.createEvent { handlers ->
        InitializeArchetypeEvent { block, builder -> handlers.forEach { it(block, builder) } }
    }

    fun interface InitializeArchetypeEvent {
        operator fun invoke(block: Block, builder: ArchetypeBuilder)
    }
}