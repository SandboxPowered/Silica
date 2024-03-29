package org.sandboxpowered.silica.api.util.extensions

import com.artemis.*
import com.artemis.utils.Bag
import com.artemis.utils.IntBag
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import net.mostlyoriginal.api.utils.pooling.ReflectionPool

inline fun <reified T : Component> World.getMapper(): ComponentMapper<T> = this.getMapper(T::class.java)

inline fun <reified T : Poolable> getPool(): ObjectPool<T> =
    ReflectionPool(T::class.java) //Pools.getPool(T::class.java)

inline fun <reified T> bag(capacity: Int): Bag<T> = Bag(T::class.java, capacity)
inline fun <reified T> bag(): Bag<T> = Bag(T::class.java)

operator fun <T> Bag<in T>.plusAssign(e: T) = this.add(e)

operator fun IntBag.plusAssign(e: Int) = this.add(e)

inline fun <reified T> WorldConfiguration.registerAs(it: T): WorldConfiguration =
    this.register(T::class.qualifiedName, it)

inline fun <reified T : Component> ArchetypeBuilder.add(): ArchetypeBuilder = add(T::class.java)

inline fun <reified T : Component> ArchetypeBuilder.remove(): ArchetypeBuilder = remove(T::class.java)

inline fun <reified T : Component> EntityEdit.create(): T = create(T::class.java)
inline fun <reified T : Component> EntityEdit.remove(): EntityEdit = remove(T::class.java)

inline fun <reified T : Component> Entity.getComponent(): T? = getComponent(T::class.java)

inline fun <reified T : BaseSystem> World.getSystem(): T = getSystem(T::class.java)
