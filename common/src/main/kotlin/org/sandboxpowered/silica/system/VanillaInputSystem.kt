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
        val input = playerInputMapper[entityId]
        val newPos = input.wantedPosition
        val oldPos = positionMapper[entityId].pos

        if (newPos != oldPos) {
            println("Player ${input.gameProfile.name} moved to $newPos")
        }

        oldPos.set(newPos)
    }
}