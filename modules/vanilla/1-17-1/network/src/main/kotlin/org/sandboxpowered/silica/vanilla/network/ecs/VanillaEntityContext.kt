package org.sandboxpowered.silica.vanilla.network.ecs

import org.joml.Vector3d
import org.sandboxpowered.silica.api.ecs.component.RotationComponent
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent

data class VanillaEntityContext(val input: VanillaPlayerInputComponent, val pos: Vector3d, val rot: RotationComponent) :
    EntityContext {
    override val sneaking: Boolean
        get() = input.sneaking
    override val horizontalFacing: Direction
        get() = Direction.fromYRotation(rot.yaw)
}
