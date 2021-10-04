package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.world.state.block.BlockState
import kotlin.math.floor

class VanillaWorldAdapter(val world: SilicaWorld) {
    private val vanillaChunkSectionMap = HashMap<ChunkSectionPos, VanillaChunkSection>()

    fun getVanillaChunkSection(pos: ChunkSectionPos): VanillaChunkSection {
        return vanillaChunkSectionMap.computeIfAbsent(pos) {
            VanillaChunkSection(world.getTerrain(), it.x * 16, it.y * 16, it.z * 16) { state ->
                world.server.stateRemapper.toVanillaId(state)
            }
        }
    }

    fun propagateUpdate(pos: Position, oldState: BlockState, newState: BlockState) {
        val chunkPos = pos.toChunkSection
        if (vanillaChunkSectionMap.contains(chunkPos)) {
            vanillaChunkSectionMap[chunkPos] =
                VanillaChunkSection(world.getTerrain(), chunkPos.x * 16, chunkPos.y * 16, chunkPos.z * 16) { state ->
                    world.server.stateRemapper.toVanillaId(state)
                }
        }
    }
}

data class ChunkSectionPos(val x: Int, val y: Int, val z: Int)

private val Position.toChunkSection: ChunkSectionPos
    get() {
        return ChunkSectionPos((x / 16f).floorToInt(), (y / 16f).floorToInt(), (z / 16f).floorToInt())
    }

private fun Float.floorToInt(): Int = floor(this).toInt()
