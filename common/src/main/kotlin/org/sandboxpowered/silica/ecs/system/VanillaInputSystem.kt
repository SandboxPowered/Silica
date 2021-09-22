package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.ecs.component.PositionComponent
import org.sandboxpowered.silica.ecs.component.RotationComponent
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.play.clientbound.UpdateEntityPosition
import org.sandboxpowered.silica.vanilla.network.play.clientbound.UpdateEntityPositionRotation
import org.sandboxpowered.silica.vanilla.network.play.clientbound.UpdateEntityRotation
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.extensions.component1
import org.sandboxpowered.silica.util.extensions.component2
import org.sandboxpowered.silica.util.extensions.component3

@All(VanillaPlayerInput::class, PositionComponent::class, RotationComponent::class)
class VanillaInputSystem(val server: SilicaServer) : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var playerInputMapper: ComponentMapper<VanillaPlayerInput>

    @Wire
    private lateinit var rotationMapper: ComponentMapper<RotationComponent>

    override fun process(entityId: Int) {
        // TODO: check if in range
        val input = playerInputMapper[entityId]
        val location = input.wantedPosition
        val yaw = input.wantedYaw
        val pitch = input.wantedPitch
        val previousLocation = positionMapper[entityId].pos
        val rot = rotationMapper[entityId]
        val (previousYaw, previousPitch) = rot

        val hasMoved = location != previousLocation
        val hasRotated = yaw != previousYaw || pitch == previousPitch

        val (x, y, z) = location

        var dx: Double = x * 32 - previousLocation.x * 32
        var dy: Double = y * 32 - previousLocation.y * 32
        var dz: Double = z * 32 - previousLocation.z * 32

        dx *= 128
        dy *= 128
        dz *= 128

        val teleport =
            dx > Short.MAX_VALUE || dy > Short.MAX_VALUE || dz > Short.MAX_VALUE || dx < Short.MIN_VALUE || dy < Short.MIN_VALUE || dz < Short.MIN_VALUE

        if (hasMoved && teleport) {

        }

        var packet: PacketPlay? = null

        if (hasRotated) {
            packet = if (hasMoved && !teleport) {
                UpdateEntityPositionRotation(entityId, dx.toInt(), dy.toInt(), dz.toInt(), yaw, pitch, false)
            } else {
                UpdateEntityRotation(entityId, yaw, pitch, false)
            }
        } else if (hasMoved) {
            packet = UpdateEntityPosition(entityId, dx.toInt(), dy.toInt(), dz.toInt(), false)
        }

        if (packet != null) {
            server.network.tell(
                Network.SendToAllExcept(
                    input.gameProfile.id,
                    packet
                )
            )
        }
        previousLocation.set(location)
    }
}