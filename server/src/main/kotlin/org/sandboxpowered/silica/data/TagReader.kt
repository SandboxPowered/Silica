package org.sandboxpowered.silica.data

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.graph.GraphBuilder
import com.google.common.graph.Traverser
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.data.jackson.IdentifierDeserializer
import org.sandboxpowered.silica.data.jackson.jsonMapperForReading

class TagReader : DataReaderWithSubCategories<TagDefinition>(
    category = "tags",
    logLoadingErrors = true,
    jacksonTypeRef()
) {

    override val om = jsonMapperForReading(
        SimpleModule().addDeserializer(Identifier::class, IdentifierDeserializer)
    )

    override fun process(data: Map<String, List<TagDefinition>>) {
        data["items"]?.let(Registries.ITEMS::addToRegistry)
        data["blocks"]?.let(Registries.BLOCKS::addToRegistry)
        data["fluids"]?.let(Registries.FLUIDS::addToRegistry)
        data["entity_types"]?.let(Registries.ENTITY_DEFINITIONS::addToRegistry)
        data["game_events"]?.let(Registries.GAME_EVENTS::addToRegistry)
        // TODO: Tags for worldgen (might need special handling of subfolders !)
    }
}

data class TagDefinition(
    val identifier: Identifier,
    val replace: Boolean = false,
    val values: Collection<Identifier>
)

@Suppress("UnstableApiUsage") // Guava Graph stuff is declared unstable
private fun Registry<*>.addToRegistry(definitions: List<TagDefinition>) {
    val tags = definitions.fold(mutableMapOf<Identifier, MutableList<Identifier>>()) { acc, definition ->
        if (definition.replace) acc[definition.identifier] = definition.values.toMutableList()
        else acc.getOrPut(definition.identifier, ::mutableListOf) += definition.values
        acc
    }
    val gb = GraphBuilder.directed().allowsSelfLoops(false).immutable<Identifier>()
    val resolved = mutableSetOf<Identifier>()
    tags.forEach { (tag, entries) ->
        var isResolved = true
        entries.removeIf {
            if (it.namespace.startsWith('#')) {
                gb.putEdge(tag, it.removeNamespacePrefix("#"))
                isResolved = false
                true
            } else false
        }
        if (isResolved) resolved += tag
    }
    val graph = gb.build()
    val traverser = Traverser.forGraph(graph)
    traverser.depthFirstPostOrder(graph.nodes().filter { it !in resolved }).forEach { child ->
        graph.predecessors(child).forEach { parent ->
            tags[parent]!!.addAll(tags[child]!!)
        }
    }
    registerTags(tags)
}
