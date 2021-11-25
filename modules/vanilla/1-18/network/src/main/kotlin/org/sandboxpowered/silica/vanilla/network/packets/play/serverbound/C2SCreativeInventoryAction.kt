package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.SlotData
import org.sandboxpowered.silica.vanilla.network.packets.play.readSlot
import org.sandboxpowered.silica.vanilla.network.packets.play.writeSlot

class C2SCreativeInventoryAction(
    private val slotId: Short,
    private val stack: SlotData
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readShort(), buf.readSlot())

    override fun write(buf: PacketBuffer) {
        buf.writeShort(slotId)
        buf.writeSlot(stack)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO: this should go through PlayerInput and check for creative
        if (slotId.toInt() == -1) logger.info("Throwing $stack")
        else context.mutatePlayerInventory {
            it[slotId.toInt()] = if (stack.present) ItemStack(
                context.idToItem(stack.itemId),
                stack.itemCount.toInt()
            ) else ItemStack.EMPTY
        }
    }
}
/*
From the wiki :

While the user is in the standard inventory (i.e., not a crafting bench) in
Creative mode, the player will send this packet.

Clicking in the creative inventory menu is quite different from non-creative
inventory management. Picking up an item with the mouse actually deletes the
item from the server, and placing an item into a slot or dropping it out of
the inventory actually tells the server to create the item from scratch. (This
can be verified by clicking an item that you don't mind deleting, then severing
the connection to the server; the item will be nowhere to be found when you log
back in.) As a result of this implementation strategy, the "Destroy Item" slot
is just a client-side implementation detail that means "I don't intend to
recreate this item.". Additionally, the long listings of items (by category,
etc.) are a client-side interface for choosing which item to create. Picking
up an item from such listings sends no packets to the server; only when you
put it somewhere does it tell the server to create the item in that location.

This action can be described as "set inventory slot". Picking up an item sets
the slot to item ID -1. Placing an item into an inventory slot sets the slot to
the specified item. Dropping an item (by clicking outside the window) effectively
sets slot -1 to the specified item, which causes the server to spawn the item
entity, etc.. All other inventory slots are numbered the same as the non-creative
inventory (including slots for the 2x2 crafting menu, even though they aren't
visible in the vanilla client).

---

Let's see if we can *not* honor this. It leads to strange behaviour if your mod
has items whose NBT you don't sync fully (the client's NBT will override the
serverside NBT, and you'll lose data -- but you might not want to send *all* nbt
to the client for security purposes (hidden data, ...))
 */