package org.sandboxpowered.silica.ecs.system

import com.artemis.BaseEntitySystem
import com.artemis.annotations.All
import org.sandboxpowered.silica.api.ecs.component.MarkForRemovalComponent
import org.sandboxpowered.silica.api.entity.EntityEvents

@All(MarkForRemovalComponent::class)
class EntityRemovalSystem : BaseEntitySystem() {

    override fun processSystem() {
        if (entityIds.size() > 0) {
            val ids = entityIds.data.copyOfRange(0, entityIds.size())

            ids.forEach(world::delete)
            EntityEvents.REMOVE_ENTITIES_EVENT.dispatcher?.invoke(ids)
        }
    }
}