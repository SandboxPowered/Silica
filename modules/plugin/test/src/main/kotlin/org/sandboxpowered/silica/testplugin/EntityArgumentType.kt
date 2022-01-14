package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.extensions.read
import org.sandboxpowered.utilities.Identifier
import java.util.concurrent.CompletableFuture

class EntityArgumentType : ArgumentType<EntityDefinition> {
    companion object {
        private val INVALID_ENTITY_TYPE = DynamicCommandExceptionType { input: Any ->
            LiteralMessage("Unknown entity '$input'")
        }
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        Registries.ENTITY_DEFINITIONS.values.forEach { (id, _) ->
            if (id.toString().startsWith(builder.remaining.lowercase())) {
                builder.suggest(id.toString())
            } else if (id.path.startsWith(builder.remaining.lowercase())) {
                builder.suggest(id.toString())
            }
        }
        return builder.buildFuture()
    }

    override fun parse(reader: StringReader): EntityDefinition {
        val start = reader.cursor
        val id = Identifier.read(reader)
        val entity = Registries.ENTITY_DEFINITIONS[id]
        if (!entity.isPresent) {
            reader.cursor = start
            throw INVALID_ENTITY_TYPE.createWithContext(reader, id)
        }
        return entity.get()
    }
}