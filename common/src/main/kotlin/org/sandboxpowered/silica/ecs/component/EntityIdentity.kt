package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.api.entity.EntityDefinition
import java.util.*

class EntityIdentity : PooledComponent() {
    var uuid: UUID? = null
    var entityDefinition: EntityDefinition? = null

    override fun reset() {
        uuid = null
        entityDefinition = null
    }
}