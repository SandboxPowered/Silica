package org.sandboxpowered.silica.vanilla.world

import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.generation.WorldGenerator

object VanillaWorldGenerator : WorldGenerator {
    override val id: Identifier = Identifier("minecraft", "vanilla")

    override val minWorldHeight: Int = 0
    override val maxWorldHeight: Int = 255
}