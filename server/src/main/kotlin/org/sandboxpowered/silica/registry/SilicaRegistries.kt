package org.sandboxpowered.silica.registry

import com.artemis.BaseEntitySystem
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.fluid.Fluid
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.RecipeType
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryDelegate
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.Identifier
import kotlin.reflect.KClass

@Suppress("unused")
object SilicaRegistries {

    private val registries: MutableMap<KClass<out RegistryEntry<*>>, Registry<*>> = Reference2ObjectLinkedOpenHashMap()

    private val BLOCK_REGISTRY = registry<Block>("minecraft", "block").apply {
        addListener {
            if (it is BlockEntityProvider)
                BLOCKS_WITH_ENTITY.add(it)
        }
    }

    val BLOCKS_WITH_ENTITY = ArrayList<BlockEntityProvider>()

    private val ITEM_REGISTRY = registry<Item>(path = "item")

    private val ENTITY_DEFINITION_REGISTRY = registry<EntityDefinition>(path = "entity_type")

    private val FLUID_REGISTRY = registry<Fluid>(path = "fluid")

    private val RECIPE_TYPE_REGISTRY = registry<RecipeType>("silica", "recipe_type")

    private val RECIPE_REGISTRY = registry<Recipe>("silica", "recipe")

    val SYSTEM_REGISTRY = mutableSetOf<BaseEntitySystem>() // TODO : make an actual registry for this
    val DYNAMIC_SYSTEM_REGISTRY =
        mutableSetOf<(Server) -> BaseEntitySystem>() // TODO : make an actual registry for this

    private val blockDelegates = HashMap<String, RegistryDelegate<Block>>()
    private val itemDelegates = HashMap<String, RegistryDelegate<Item>>()
    private val fluidDelegates = HashMap<String, RegistryDelegate<Fluid>>()

    fun blocks(domain: String = "minecraft"): RegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCK_REGISTRY, it) }

    fun items(domain: String = "minecraft"): RegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEM_REGISTRY, it) }

    fun fluids(domain: String = "minecraft"): RegistryDelegate<Fluid> =
        fluidDelegates.computeIfAbsent(domain) { RegistryDelegate(FLUID_REGISTRY, it) }

    private inline fun <reified T : RegistryEntry<T>> registry(
        namespace: String = "minecraft",
        path: String
    ): SilicaRegistry<T> {
        val registry = SilicaRegistry(Identifier(namespace, path), T::class.java)
        registries[T::class] = registry
        return registry
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : RegistryEntry<T>> get(clazz: KClass<T>): Registry<T> =
        registries[clazz] as Registry<T>? ?: error("Unknown registry type $clazz")
}