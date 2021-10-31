package org.sandboxpowered.silica.ecs.system

import com.artemis.BaseEntitySystem
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import net.mostlyoriginal.api.event.common.EventSystem
import org.sandboxpowered.silica.ecs.component.MarkForRemovalComponent
import org.sandboxpowered.silica.ecs.events.RemoveEntitiesEvent

@All(MarkForRemovalComponent::class)
class EntityRemovalSystem : BaseEntitySystem() {

    @Wire
    private lateinit var eventSystem: EventSystem

    override fun processSystem() {
        val ids = entityIds.data.copyOfRange(0, entityIds.size())
        ids.forEach(world::delete)
        eventSystem.dispatch(RemoveEntitiesEvent(ids))
    }
}