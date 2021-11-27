package org.sandboxpowered.silica.api.entity

import com.artemis.ArchetypeBuilder
import com.artemis.Entity
import org.sandboxpowered.silica.api.entity.EntityEvents.InitializeArchetypeEvent
import org.sandboxpowered.silica.api.entity.EntityEvents.ChangeChunkEvent
import org.sandboxpowered.silica.api.entity.EntityEvents.RemoveEntitiesEvent
import org.sandboxpowered.silica.api.entity.EntityEvents.SpawnEntityEvent
import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory
import org.sandboxpowered.silica.api.util.math.ChunkPosition

object EntityEvents {
    val INITIALIZE_ARCHETYPE_EVENT: Event<InitializeArchetypeEvent> = EventFactory.createEvent { handlers ->
        InitializeArchetypeEvent { entityDefinition, builder -> handlers.forEach { it(entityDefinition, builder) } }
    }
    val SPAWN_ENTITY_EVENT: Event<SpawnEntityEvent> = EventFactory.createEvent { handlers ->
        SpawnEntityEvent { ent -> handlers.forEach { it(ent) } }
    }
    val REMOVE_ENTITIES_EVENT: Event<RemoveEntitiesEvent> = EventFactory.createEvent { handlers ->
        RemoveEntitiesEvent { ent -> handlers.forEach { it(ent) } }
    }
    val CHANGE_CHUNK_EVENT: Event<ChangeChunkEvent> = EventFactory.createEvent { handlers ->
        ChangeChunkEvent { ent, old, new -> handlers.forEach { it(ent, old, new) } }
    }

    fun interface InitializeArchetypeEvent {
        operator fun invoke(entityDefinition: EntityDefinition, builder: ArchetypeBuilder)
    }

    fun interface SpawnEntityEvent {
        operator fun invoke(entity: Entity)
    }

    fun interface RemoveEntitiesEvent {
        operator fun invoke(entities: IntArray)
    }

    fun interface ChangeChunkEvent {
        operator fun invoke(player: Int, old: ChunkPosition, new: ChunkPosition)
    }
}