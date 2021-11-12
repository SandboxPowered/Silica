package org.sandboxpowered.silica.vanilla.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.api.ecs.component.MarkForRemovalComponent
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.ecs.component.EntityTestComponent

@All(EntityTestComponent::class)
class EntityTestSystem : IteratingSystem() {

    @Wire
    private lateinit var testMapper: ComponentMapper<EntityTestComponent>

    @Wire
    private lateinit var removalMapper: ComponentMapper<MarkForRemovalComponent>

    private val logger = getLogger()

    override fun process(entityId: Int) {
        if (--testMapper[entityId].ttl <= 0) {
            logger.info("Killing entity $entityId")
            removalMapper.create(entityId)
        }
    }
}