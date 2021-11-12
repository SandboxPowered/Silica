package org.sandboxpowered.silica.api.entity

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory

object EntityEvents {
    val INITIALIZE_ARCHETYPE_EVENT: Event<InitializeArchetypeEvent> = EventFactory.createEvent { handlers ->
        InitializeArchetypeEvent { entityDefinition, builder -> handlers.forEach { it(entityDefinition, builder) } }
    }
}

fun interface InitializeArchetypeEvent {
    operator fun invoke(entityDefinition: EntityDefinition, builder: ArchetypeBuilder)
}