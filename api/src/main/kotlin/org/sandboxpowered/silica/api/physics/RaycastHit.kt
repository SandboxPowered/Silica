package org.sandboxpowered.silica.api.physics

sealed class RaycastHit {
    object Miss : RaycastHit()
}