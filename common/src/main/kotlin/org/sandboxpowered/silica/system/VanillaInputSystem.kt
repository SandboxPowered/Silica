package org.sandboxpowered.silica.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.component.PositionComponent
import org.sandboxpowered.silica.component.VanillaPlayerInput

@All(VanillaPlayerInput::class, PositionComponent::class)
class VanillaInputSystem : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>
    @Wire
    private lateinit var playerInputMapper: ComponentMapper<VanillaPlayerInput>

    override fun process(entityId: Int) {
        // TODO: check if in range
        positionMapper[entityId].pos.set(playerInputMapper[entityId].wantedPosition)
    }
}