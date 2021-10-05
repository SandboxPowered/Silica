package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.world.state.block.BlockState
import kotlin.random.Random

interface BakedModel {
    fun getQuads(state: BlockState, face: Direction?, rand: Random): Collection<BakedQuad>
}