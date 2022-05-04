package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.joml.Vector3f
import org.sandboxpowered.silica.api.ecs.component.HitboxComponent
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.ecs.component.VelocityComponent
import org.sandboxpowered.silica.api.physics.AxisAlignedBox
import org.sandboxpowered.silica.api.physics.walkCorners
import org.sandboxpowered.silica.api.util.extensions.component1
import org.sandboxpowered.silica.api.util.extensions.component2
import org.sandboxpowered.silica.api.util.extensions.component3
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.utilities.math.times
import kotlin.math.min

@All(PositionComponent::class, VelocityComponent::class)
class PhysicsSystem : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var velocityMapper: ComponentMapper<VelocityComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var entity3dMap: Entity3dMap

    @Wire
    private lateinit var worldReader: World

    override fun process(entityId: Int) {
        val position = positionMapper[entityId].pos
        val velocityComponent = velocityMapper[entityId]
        val nDirection = velocityComponent.direction.normalize()

        if (hitboxMapper.has(entityId)) {
            val (x, y, z) = position
            val (w, h, d) = hitboxMapper[entityId].hitbox

            val bounds = AxisAlignedBox(
                (x - w / 2).toFloat(), y.toFloat() + .1f, (z - d / 2).toFloat(), w, h, d
            )

            val worldSection = worldReader.subsection(
                bounds.x.toInt(), bounds.y.toInt(), bounds.z.toInt(),
                bounds.w.toInt() + 1, bounds.h.toInt() + 1, bounds.d.toInt() + 1
            )

            var min = -1f
            bounds.walkCorners { cx, cy, cz ->
                val hit = worldSection.rayCast(Vector3f(cx, cy, cz), nDirection, velocityComponent.velocity)
                if (hit == 0f) return // TODO: try to unstuck the poor thing (as this means one or more of it's corners are inside a non air block)
                else if (hit > 0f) min = if (min < 0f) hit else min(min, hit)
            }

            position.add(nDirection * if (min < 0) velocityComponent.velocity else min)

        } else position.add(nDirection * velocityComponent.velocity)

    }
}