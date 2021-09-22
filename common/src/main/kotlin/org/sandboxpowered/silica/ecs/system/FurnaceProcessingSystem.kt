package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.ecs.component.BlockPositionComponent
import org.sandboxpowered.silica.ecs.component.FurnaceLogicComponent


@All(BlockPositionComponent::class, FurnaceLogicComponent::class)
class FurnaceProcessingSystem : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<BlockPositionComponent>

    @Wire
    private lateinit var logicMapper: ComponentMapper<FurnaceLogicComponent>

    override fun process(entityId: Int) {
        TODO("Not yet implemented")
    }
}