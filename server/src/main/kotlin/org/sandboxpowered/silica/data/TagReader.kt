package org.sandboxpowered.silica.data

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
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
    }
}

data class TagDefinition(
    val identifier: Identifier,
    val replace: Boolean = false,
    val values: Collection<Identifier>
)

private fun Registry<*>.addToRegistry(definitions: List<TagDefinition>) {
    // TODO : values can point to other tags
    registerTags(definitions.fold(mutableMapOf<Identifier, MutableList<Identifier>>()) { acc, definition ->
        if (definition.replace) acc[definition.identifier] = definition.values.toMutableList()
        else acc.getOrPut(definition.identifier, ::mutableListOf) += definition.values
        acc
    })
}
