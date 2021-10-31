package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent

abstract class SingletonComponent : PooledComponent() {
    override fun reset() = Unit
}

class MarkForRemovalComponent : SingletonComponent()