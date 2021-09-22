package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import com.mojang.authlib.GameProfile

class PlayerComponent(var profile: GameProfile? = null) : PooledComponent() {
    override fun reset() {
        profile = null
    }
}