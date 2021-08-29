package org.sandboxpowered.silica.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.artemis.utils.IntBag
import org.joml.Vector3fc
import org.sandboxpowered.silica.component.HitboxComponent
import org.sandboxpowered.silica.component.PlayerComponent
import org.sandboxpowered.silica.component.PositionComponent
import org.sandboxpowered.silica.util.extensions.component1
import org.sandboxpowered.silica.util.extensions.component2
import org.sandboxpowered.silica.util.extensions.component3
import org.sandboxpowered.silica.world.util.OcTree

/**
 * [Wire] this to get spatial lookup for entities
 */
interface Entity3dMap {

    /**
     * Get an [IntBag] containing the IDs of all living entities within [box] at [pos]
     */
    fun getLiving(pos: Vector3fc, box: Vector3fc): IntBag

    /**
     * Get an [IntBag] containing the IDs of all players within [box] at [pos]
     */
    fun getPlayers(pos: Vector3fc, box: Vector3fc): IntBag
}

@All(PositionComponent::class, HitboxComponent::class)
class Entity3dMapSystem(
    private val tree: OcTree
) : IteratingSystem(), Entity3dMap {

    private val livingFlag = tree.nextFlag()
    private val playerFlag = tree.nextFlag() or livingFlag

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var playerMapper: ComponentMapper<PlayerComponent>

    override fun inserted(entityId: Int) {
        val isPlayer = playerMapper.has(entityId)
        val (x, y, z) = positionMapper[entityId].pos
        val (w, h, d) = hitboxMapper[entityId].hitbox

        tree.insert(
            entityId, if (isPlayer) playerFlag else livingFlag /*TODO: improve this shit*/,
            x.toFloat(), y.toFloat(), z.toFloat(),
            w, h, d
        )
    }

    override fun removed(entityId: Int) {
        tree.remove(entityId)
    }

    override fun process(entityId: Int) {
        val (x, y, z) = positionMapper[entityId].pos
        val (w, h, d) = hitboxMapper[entityId].hitbox

        tree.update(
            entityId,
            x.toFloat(), y.toFloat(), z.toFloat(),
            w, h, d
        )
    }

    override fun getLiving(pos: Vector3fc, box: Vector3fc) = get(pos, box, livingFlag)

    override fun getPlayers(pos: Vector3fc, box: Vector3fc) = get(pos, box, playerFlag)

    private fun get(pos: Vector3fc, box: Vector3fc, flags: Long): IntBag {
        val (x, y, z) = pos
        val (w, h, d) = box

        return tree.getExact(IntBag(), x, y, z, w, h, d, flags)
    }
}
