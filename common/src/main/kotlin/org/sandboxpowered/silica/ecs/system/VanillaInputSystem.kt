package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.joml.Vector3d
import org.sandboxpowered.silica.api.ecs.PositionComponent
import org.sandboxpowered.silica.api.item.BlockItem
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.util.extensions.component1
import org.sandboxpowered.silica.api.util.extensions.component2
import org.sandboxpowered.silica.api.util.extensions.component3
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.content.block.Blocks
import org.sandboxpowered.silica.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.ecs.component.RotationComponent
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.server.VanillaNetwork
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CUpdateEntityPosition
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CUpdateEntityPositionRotation
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CUpdateEntityRotation

@All(VanillaPlayerInput::class, PositionComponent::class, RotationComponent::class)
class VanillaInputSystem(val server: SilicaServer) : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var playerInputMapper: ComponentMapper<VanillaPlayerInput>

    @Wire
    private lateinit var rotationMapper: ComponentMapper<RotationComponent>

    @Wire
    private lateinit var inventoryMapper: ComponentMapper<PlayerInventoryComponent>

    @Wire
    private lateinit var terrain: World

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
                S2CUpdateEntityPositionRotation(entityId, dx.toInt(), dy.toInt(), dz.toInt(), yaw, pitch, false)
            } else {
                S2CUpdateEntityRotation(entityId, yaw, pitch, false)
            }
        } else if (hasMoved) {
            packet = S2CUpdateEntityPosition(entityId, dx.toInt(), dy.toInt(), dz.toInt(), false)
        }

        if (packet != null) {
            server.vanillaNetwork.tell(
                VanillaNetwork.SendToAllExcept(
                    input.gameProfile.id,
                    packet
                )
            )
        }
        previousLocation.set(location)

        this.handleTerrainInteraction(entityId, input, previousLocation)
    }

    private fun handleTerrainInteraction(entityId: Int, input: VanillaPlayerInput, position: Vector3d) {
        input.breaking = performWithRangedCheck(input.breaking, position) {
            terrain.setBlockState(it, Blocks.AIR.defaultState)
        }

        input.placing = performWithRangedCheck(input.placing, position) {
            val heldItem = inventoryMapper[entityId]?.inventory?.mainHandStack?.takeUnless(ItemStack::isEmpty)
            if (heldItem != null) {
                val item = heldItem.item
                if (item is BlockItem) {
                    terrain.setBlockState(it, item.block.defaultState)
                }
            }
        }
    }

    private inline fun performWithRangedCheck(
        target: Position?,
        position: Vector3d,
        perform: (Position) -> Unit
    ): Position? {
        if (target != null && position.distanceSquared(target.x + .5, target.y + .5, target.z + .5) < 20) {
            perform(target)
        }
        return null
    }
}