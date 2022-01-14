package org.sandboxpowered.silica.api.world.generation

import org.sandboxpowered.utilities.Identifier

interface WorldGenerator {
    val id: Identifier

    val minWorldWidth: Int
    val maxWorldWidth: Int
    val minWorldHeight: Int
    val maxWorldHeight: Int

    val width: Int get() = -minWorldWidth + maxWorldWidth + 1
    val height: Int get() = -minWorldHeight + maxWorldHeight + 1
}