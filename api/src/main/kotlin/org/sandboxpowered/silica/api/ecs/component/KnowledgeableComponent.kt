package org.sandboxpowered.silica.api.ecs.component

import com.artemis.Component
import com.artemis.Entity

class KnowledgeableComponent : Component() {
    lateinit var entity: Entity

    inline fun <reified T : Component> tryGetComponent(): T? = entity.getComponent(T::class.java)
    inline fun <reified T : Component> getComponent(): T =
        entity.getComponent(T::class.java) ?: error("Entity ${entity.id} does not contain component ${T::class.java}")
}