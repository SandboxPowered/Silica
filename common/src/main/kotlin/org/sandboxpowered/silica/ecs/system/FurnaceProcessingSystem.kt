package org.sandboxpowered.silica.ecs.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.DelayedIteratingSystem
import org.sandboxpowered.silica.content.inventory.ResizableInventory
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.ecs.component.BlockPositionComponent
import org.sandboxpowered.silica.ecs.component.FurnaceLogicComponent
import org.sandboxpowered.silica.ecs.component.ResizableInventoryComponent
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier
import kotlin.math.min


@All(BlockPositionComponent::class, FurnaceLogicComponent::class, ResizableInventoryComponent::class)
class FurnaceProcessingSystem : DelayedIteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<BlockPositionComponent>

    @Wire
    private lateinit var logicMapper: ComponentMapper<FurnaceLogicComponent>

    @Wire
    private lateinit var inventoryMapper: ComponentMapper<ResizableInventoryComponent>

    fun resetIfApplicable(inventory: ResizableInventory) {
        if (inventory.size != 3)
            inventory.reset(3)
    }

    val fuelTime = 1600 // TODO replace with getting fuel per item
    val smeltTime = 200 // TODO replace with getting fuel per recipe

    override fun getRemainingDelay(entityId: Int): Float {
        val logic = logicMapper[entityId]
        return min(logic.fuelTime, logic.smeltingTime)
    }

    override fun processDelta(entityId: Int, accumulatedDelta: Float) {
        val logic = logicMapper[entityId]
        logic.fuelTime -= accumulatedDelta
        logic.smeltingTime -= accumulatedDelta
    }


    // TODO replace with actual recipes

    private val ironOre by SilicaRegistries.ITEM_REGISTRY[Identifier.of("iron_ore")].guarentee()
    private val ironIngot by SilicaRegistries.ITEM_REGISTRY[Identifier.of("iron_ingot")].guarentee()
    private val outputStack: ItemStack by lazy {
        ItemStack.of(ironIngot, 2)
    }

    override fun processExpired(entityId: Int) {
        val inventory = inventoryMapper[entityId].inventory.apply(this::resetIfApplicable)
        val logic = logicMapper[entityId]

        var isBurning = logic.fuelTime > 0
        val (inputItem, fuelItem, outputItem) = inventory
        if ((isBurning || !fuelItem.isEmpty) && !inputItem.isEmpty) {
            val isValidItemForRecipe = inputItem.isItemEqual(ironOre)
            if (!isValidItemForRecipe) {
                val canAcceptRecipeOutput =
                    outputItem.isEmpty || (outputItem.isItemEqual(ironIngot) && outputItem.count + outputStack.count <= 64)
                if (!isBurning && canAcceptRecipeOutput) {
                    logic.fuelTime = fuelTime / 20f
                    fuelItem -= 1
                    isBurning = true
                    //TODO support recipe remainders (lava bucket -> empty bucket)
                }

                if (isBurning && canAcceptRecipeOutput) {
                    if (logic.smeltingTime <= 0) {
                        logic.smeltingTime = 0f
                        if (outputItem.isEmpty) {
                            inventory[2] = outputStack.duplicate()
                        } else {
                            outputItem += outputStack.count
                        }
                    }
                } else {
                    logic.smeltingTime = 0f
                }
            }
        }

        // TODO update blockstate in world
    }
}

private operator fun ResizableInventory.component1(): ItemStack = get(0)
private operator fun ResizableInventory.component2(): ItemStack = get(1)
private operator fun ResizableInventory.component3(): ItemStack = get(2)
