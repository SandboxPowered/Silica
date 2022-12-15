package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.writeCollection
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.SlotData
import org.sandboxpowered.silica.vanilla.network.packets.play.writeSlot
import org.sandboxpowered.silica.vanilla.recipe.ShapedCraftingRecipe
import org.sandboxpowered.silica.vanilla.recipe.ShapelessCraftingRecipe
import org.sandboxpowered.silica.vanilla.recipe.SimpleProcessingRecipe
import org.sandboxpowered.silica.vanilla.recipe.StoneCuttingRecipe
import kotlin.system.measureTimeMillis

class S2CDeclareRecipes(
    private val recipes: Collection<Recipe>
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(emptyList())

    private val logger = getLogger()

    override fun write(buf: PacketBuffer) {
        val time = measureTimeMillis {
            buf.writeCollection(recipes, PacketBuffer::write)
        }

        logger.info("Serialized recipes in $time millis")
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}

private fun PacketBuffer.write(recipe: Recipe): PacketBuffer {
    // TODO : maybe codecs isn't so bad after all~
    writeIdentifier(recipe.type)
    writeIdentifier(recipe.identifier)
    when (recipe) {
        is ShapelessCraftingRecipe -> {
            writeString(recipe.group ?: "unknown")
            writeCollection(recipe.ingredients, PacketBuffer::write)
            writeSlot(SlotData.from(recipe.result))
        }

        is ShapedCraftingRecipe -> {
            writeVarInt(recipe.width)
            writeVarInt(recipe.height)
            writeString(recipe.group ?: "unknown")
            // Not writing as collection here because otherwise it would insert size again while it's computed from WxH
            recipe.pattern.asSequence().flatMap { line ->
                line.map {
                    recipe.key[it] ?: Ingredient.Tag(Identifier("empty hack"))
                }
            }.fold(this, PacketBuffer::write)
            writeSlot(SlotData.from(recipe.result))
        }

        is SimpleProcessingRecipe -> {
            writeString(recipe.group ?: "unknown")
            write(recipe.ingredient)
            writeSlot(SlotData.from(recipe.result))
            writeFloat(recipe.experience)
            writeVarInt(recipe.cookingTime)
        }

        is StoneCuttingRecipe -> {
            writeString(recipe.group ?: "unknown")
            write(recipe.ingredient)
            writeSlot(SlotData.from(recipe.result))
        }

        else -> error("Can't serialize ${recipe.identifier} of type ${recipe.type}")
    }

    return this
}

private fun PacketBuffer.write(ingredient: Ingredient): PacketBuffer {
    when (ingredient) {
        is Ingredient.Composite -> {
            writeCollection(ingredient.ingredients, PacketBuffer::writeStrict)
        }

        else -> {
            writeVarInt(1)
            writeStrict(ingredient)
        }
    }
    return this
}

private fun PacketBuffer.writeStrict(ingredient: Ingredient): PacketBuffer {
    when (ingredient) {
        is Ingredient.Item -> {
            writeSlot(SlotData.from(ingredient.item))
        }

        is Ingredient.Tag -> {
            writeSlot(SlotData.EMPTY) // TODO : handle tags
        }

        is Ingredient.Composite -> error("Should not be called for composites")
    }
    return this
}
