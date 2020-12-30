package org.sandboxpowered.silica.component

import com.artemis.PooledComponent
import com.mojang.authlib.GameProfile

class PlayerComponent : PooledComponent() {
    var profile: GameProfile? = null

    override fun reset() {
        profile = null;
    }
}