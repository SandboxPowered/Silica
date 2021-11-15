package org.sandboxpowered.silica.vanilla.data

import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.ecs.component.HitboxComponent
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.entity.BaseEntityDefinition
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.add
import org.sandboxpowered.silica.vanilla.ecs.component.EntityTestComponent
import org.sandboxpowered.silica.vanilla.ecs.system.EntityTestSystem

object Entities {

    fun init() {
        register(BaseEntityDefinition(Identifier("zombie")) {
            add<PositionComponent>()
            add<EntityTestComponent>()
            add<HitboxComponent>()
        })
        register(BaseEntityDefinition(Identifier("creeper")) {
            add<PositionComponent>()
            add<EntityTestComponent>()
            add<HitboxComponent>()
        })
        SilicaAPI.registerSystem(EntityTestSystem())
    }

    private fun register(entityDefinition: EntityDefinition) {
        Registries.ENTITY_DEFINITIONS.register(entityDefinition)
    }
}