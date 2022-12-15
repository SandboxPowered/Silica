package org.sandboxpowered.silica.vanilla.world

import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.world.generation.WorldGenerator

object VanillaWorldGenerator : WorldGenerator {
    override val id: Identifier = Identifier("minecraft", "vanilla")
    override val minWorldWidth: Int = -1 shl 25
    override val maxWorldWidth: Int = (1 shl 25) - 1
    override val minWorldHeight: Int = -64
    override val maxWorldHeight: Int = 320
}