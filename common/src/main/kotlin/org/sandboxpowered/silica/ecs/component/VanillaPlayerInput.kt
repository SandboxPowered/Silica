package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import com.mojang.authlib.GameProfile
import org.joml.Vector3d
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.util.math.Position

class VanillaPlayerInput : PooledComponent() {
    private var initialized = false

    var playerId: Int = UNKNOWN_ID
        private set

    lateinit var gameProfile: GameProfile
        private set

    val wantedPosition: Vector3d = Vector3d()
    var wantedYaw: Float = 0f
    var wantedPitch: Float = 0f

    var sneaking = false
    var jumping = false

    var breaking: Position? = null
    var interacting: InteractionContext? = null

    var flying = false

    override fun reset() {
        initialized = false
        wantedPosition.set(0.0, 0.0, 0.0)
        wantedYaw = 0f
        wantedPitch = 0f
        sneaking = false
        jumping = false

        breaking = null
        interacting = null

        flying = false
    }

    internal fun initialize(playerId: Int, gameProfile: GameProfile) {
        check(!initialized) { "Input for $playerId already initialized for ${this.playerId}" }
        this.playerId = playerId
        this.gameProfile = gameProfile
        this.initialized = true
    }

    private companion object {
        private const val UNKNOWN_ID = -1
    }
}