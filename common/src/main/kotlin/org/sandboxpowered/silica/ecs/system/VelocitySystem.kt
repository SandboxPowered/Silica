package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.ecs.component.VelocityComponent

@All(PositionComponent::class, VelocityComponent::class)
class VelocitySystem : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var velocityMapper: ComponentMapper<VelocityComponent>

    override fun process(entityId: Int) {
        val velo = velocityMapper[entityId].velocity
        positionMapper[entityId].pos.add(velo) // very crude for now
    }
}