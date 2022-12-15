package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.internal.getRegistry
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.RecipeType

object Registries {
    val BLOCKS: Registry<Block> = SilicaAPI.getRegistry()
    val ITEMS: Registry<Item> = SilicaAPI.getRegistry()
    val ENTITY_DEFINITIONS: Registry<EntityDefinition> = SilicaAPI.getRegistry()
    val RECIPE_TYPES: Registry<RecipeType> = SilicaAPI.getRegistry()
    val RECIPES: Registry<Recipe> = SilicaAPI.getRegistry()
}