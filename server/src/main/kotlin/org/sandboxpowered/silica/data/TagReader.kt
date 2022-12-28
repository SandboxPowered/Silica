package org.sandboxpowered.silica.data

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
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
        // TODO: store in some registry
    }
}

data class TagDefinition(
    val identifier: Identifier,
    val replace: Boolean = false,
    val values: Collection<Identifier>
)
