package org.sandboxpowered.silica.vanilla.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.api.util.getLogger
import kotlin.random.Random

class EntityTestComponent : PooledComponent() {
    var ttl = Random.nextInt(200)

    private val logger = getLogger()

    override fun reset() {
        ttl = Random.nextInt(200)
        logger.info("Setting next ttl to $ttl")
    }
}