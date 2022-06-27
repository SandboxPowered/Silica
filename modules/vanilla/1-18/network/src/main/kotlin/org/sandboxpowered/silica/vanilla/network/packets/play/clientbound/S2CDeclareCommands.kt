package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import com.google.common.collect.Queues
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.tree.RootCommandNode
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.sandboxpowered.silica.api.command.CommandSource
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.writeCollection
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.utilities.Identifier
import kotlin.experimental.or

class S2CDeclareCommands(val root: RootCommandNode<CommandSource>? = null) : PacketPlay {
    constructor(buf: PacketBuffer) : this(TODO() as RootCommandNode<CommandSource>)

    override fun write(buf: PacketBuffer) {
        val root = root!!
        val nodes = enumerateNodes(root)
        val orderedList = orderNodes(nodes)
        buf.writeCollection(orderedList) {
            writeNode(it, nodes)
        }
        buf.writeVarInt(nodes.getInt(root))
    }

    private fun PacketBuffer.writeNode(
        node: CommandNode<CommandSource>,
        nodes: Object2IntMap<CommandNode<CommandSource>>
    ): PacketBuffer {
        var res: Byte = 0
        if (node.redirect != null) {
            res = res or 8
        }

        if (node.command != null) {
            res = res or 4
        }

        if (node is RootCommandNode) {
            res = res or 0
        } else if (node is ArgumentCommandNode<*, *>) {
            res = res or 2
            if (node.customSuggestions != null) {
                res = res or 16
            }
        } else {
            if (node !is LiteralCommandNode) {
                error("Unknown node type ${node.javaClass.simpleName}")
            }
            res = res or 1
        }

        writeByte(res)
        writeVarInt(node.children.size)

        node.children.forEach {
            writeVarInt(nodes.getInt(it))
        }

        if (node.redirect != null) {
            writeVarInt(nodes.getInt(node.redirect))
        }

        if (node is ArgumentCommandNode<*, *>) {
            writeString(node.name)
            ArgumentTypes.serialize(this, node.type)
            if (node.customSuggestions != null) {
                writeIdentifier(Identifier("ask_server"))
            }
        } else if (node is LiteralCommandNode) {
            writeString(node.literal)
        }

        return this
    }

    private fun orderNodes(nodes: Object2IntMap<CommandNode<CommandSource>>): List<CommandNode<CommandSource>> {
        val list = ObjectArrayList<CommandNode<CommandSource>>()
        list.size(nodes.size)

        nodes.forEach { (node, id) ->
            list[id] = node
        }
        return list
    }

    fun enumerateNodes(root: RootCommandNode<CommandSource>): Object2IntMap<CommandNode<CommandSource>> {
        val map = Object2IntOpenHashMap<CommandNode<CommandSource>>()
        val queue = Queues.newArrayDeque<CommandNode<CommandSource>>()
        queue.add(root)
        while (!queue.isEmpty()) {
            val node = queue.poll()
            if (node !in map) {
                map[node] = map.size
                queue.addAll(node.children)
                if (node.redirect != null) {
                    queue.add(node.redirect)
                }
            }
        }
        return map
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}