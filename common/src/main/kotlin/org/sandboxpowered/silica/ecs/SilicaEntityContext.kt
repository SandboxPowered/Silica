package org.sandboxpowered.silica.ecs

import org.joml.Vector3d
import org.sandboxpowered.silica.api.ecs.component.RotationComponent
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput

data class SilicaEntityContext(val input: VanillaPlayerInput, val pos: Vector3d, val rot: RotationComponent) :
    EntityContext {
    override val sneaking: Boolean
        get() = input.sneaking
    override val horizontalFacing: Direction
        get() = Direction.fromYRotation(rot.yaw)
}
