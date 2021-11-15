package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.vanilla.data.Blocks
import org.sandboxpowered.silica.vanilla.data.Entities
import org.sandboxpowered.silica.vanilla.data.Items

object SilicaInit {
    fun init() {
        Blocks.init()
        Items.init()
        Entities.init()
    }
}