package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.One
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.artemis.utils.IntBag
import org.joml.Vector3fc
import org.joml.Vector3ic
import org.sandboxpowered.silica.ecs.component.BlockPositionComponent
import org.sandboxpowered.silica.ecs.component.HitboxComponent
import org.sandboxpowered.silica.ecs.component.PlayerComponent
import org.sandboxpowered.silica.ecs.component.PositionComponent
import org.sandboxpowered.silica.util.extensions.component1
import org.sandboxpowered.silica.util.extensions.component2
import org.sandboxpowered.silica.util.extensions.component3
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.world.util.IntTree
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

    fun getBlockEntities(pos: Position, box: Vector3ic): IntBag

    fun getBlockEntities(pos: Position): IntBag
}

@One(BlockPositionComponent::class, PositionComponent::class)
class Entity3dMapSystem(
    private val tree: OcTree,
    private val blockEntityTree: IntTree
) : IteratingSystem(), Entity3dMap {

    private val livingFlag = tree.nextFlag()
    private val playerFlag = tree.nextFlag() or livingFlag

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var bePositionMapper: ComponentMapper<BlockPositionComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var playerMapper: ComponentMapper<PlayerComponent>

    override fun inserted(entityId: Int) {
        val isBE = bePositionMapper.has(entityId)
        if (!isBE) {
            val isPlayer = playerMapper.has(entityId)
            val (x, y, z) = positionMapper[entityId].pos
            val (w, h, d) = hitboxMapper[entityId].hitbox

            tree.insert(
                entityId, if (isPlayer) playerFlag else livingFlag /*TODO: improve this shit*/,
                x.toFloat(), y.toFloat(), z.toFloat(),
                w, h, d
            )
        } else {
            val (x, y, z) = bePositionMapper[entityId].pos

            blockEntityTree.insert(entityId, 0, x, y, z, 1, 1, 1)
        }
    }

    override fun removed(entityId: Int) {
        tree.remove(entityId)
        blockEntityTree.remove(entityId)
    }

    override fun process(entityId: Int) {
        val isBE = bePositionMapper.has(entityId)
        if (!isBE) {
            val (x, y, z) = positionMapper[entityId].pos
            val (w, h, d) = hitboxMapper[entityId].hitbox

            tree.update(
                entityId,
                x.toFloat(), y.toFloat(), z.toFloat(),
                w, h, d
            )
        } else {
            val (x, y, z) = bePositionMapper[entityId].pos

            blockEntityTree.update(entityId, x, y, z, 1, 1, 1)
        }
    }

    override fun getBlockEntities(pos: Position, box: Vector3ic): IntBag {
        val (x, y, z) = pos
        val (w, h, d) = box
        return blockEntityTree.getExact(IntBag(), x, y, z, w, h, d, 0)
    }

    override fun getBlockEntities(pos: Position): IntBag {
        val (x, y, z) = pos
        return blockEntityTree[IntBag(), x, y, z, 0]
    }

    override fun getLiving(pos: Vector3fc, box: Vector3fc) = get(pos, box, livingFlag)

    override fun getPlayers(pos: Vector3fc, box: Vector3fc) = get(pos, box, playerFlag)

    private fun get(pos: Vector3fc, box: Vector3fc, flags: Long): IntBag {
        val (x, y, z) = pos
        val (w, h, d) = box

        return tree.getExact(IntBag(), x, y, z, w, h, d, flags)
    }
}
