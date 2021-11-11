package org.sandboxpowered.silica.vanilla.ecs

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import org.sandboxpowered.silica.api.ecs.BlockPositionComponent
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.item.inventory.BaseInventory
import org.sandboxpowered.silica.api.registry.Registries.items


@All(BlockPositionComponent::class, FurnaceLogicComponent::class, ResizableInventoryComponent::class)
class FurnaceProcessingSystem : IteratingSystem() {

    @Wire
    private lateinit var positionMapper: ComponentMapper<BlockPositionComponent>

    @Wire
    private lateinit var logicMapper: ComponentMapper<FurnaceLogicComponent>

    @Wire
    private lateinit var inventoryMapper: ComponentMapper<ResizableInventoryComponent>

    fun resetIfApplicable(inventory: BaseInventory) {
        if (inventory.size != 3) inventory.clear()
    }

    // TODO replace with actual recipes
    val smeltTime = 200
    private val iron_ore by items()
    private val iron_ingot by items()
    private val outputStack: ItemStack by lazy {
        ItemStack(iron_ingot, 1)
    }

    override fun process(entityId: Int) {
        val inventory = inventoryMapper[entityId].inventory.apply(this::resetIfApplicable)
        val logic = logicMapper[entityId]

        var isBurning = logic.fuelTime > 0

        if (isBurning) {
            logic.fuelTime -= 1
        }

        val (inputItem, fuelItem, outputItem) = inventory
        if ((isBurning || fuelItem.isNotEmpty) && !inputItem.isEmpty) {
            val isValidItemForRecipe = inputItem.isItemEqual(iron_ore)
            if (isValidItemForRecipe) {
                val canAcceptRecipeOutput =
                    outputItem.isEmpty || (outputItem.isItemEqual(iron_ingot) && outputItem.count + outputStack.count <= 64)
                if (!isBurning && canAcceptRecipeOutput) {
                    logic.fuelTime = fuelItem.item.properties.fuelTime
                    logic.fuelTimeTotal = logic.fuelTime
                    fuelItem -= 1
                    isBurning = true
                    //TODO support recipe remainders (lava bucket -> empty bucket)
                }

                if (isBurning && canAcceptRecipeOutput) {
                    logic.smeltingTime++
                    if (logic.smeltingTime >= smeltTime.toFloat()) {
                        logic.smeltingTimeTotal = getTotalSmeltTime(logic, inventory)
                        logic.smeltingTime = 0
                        if (outputItem.isEmpty) {
                            inventory[2] = outputStack.duplicate()
                        } else {
                            outputItem += outputStack.count
                        }
                    }
                } else {
                    logic.smeltingTime = 0
                    logic.smeltingTimeTotal = getTotalSmeltTime(logic, inventory)
                }
            }
        }
    }

    fun getTotalSmeltTime(logic: FurnaceLogicComponent, inventory: BaseInventory): Int {
        val input = inventory[0]
        if (input.isEmpty) return 0
        return smeltTime
    }
}

private operator fun BaseInventory.component1(): ItemStack = get(0)
private operator fun BaseInventory.component2(): ItemStack = get(1)
private operator fun BaseInventory.component3(): ItemStack = get(2)
