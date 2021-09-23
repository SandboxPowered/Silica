package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.world.state.block.BlockState

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
        TODO("Not yet implemented")
    }
}

data class ChunkSectionPos(val x: Int, val y: Int, val z: Int)