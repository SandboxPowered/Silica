package org.sandboxpowered.silica.data

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.*
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.treeToValue
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.data.jackson.IdentifierDeserializer
import org.sandboxpowered.silica.data.jackson.ItemStackDeserializer
import org.sandboxpowered.silica.data.jackson.UnknownItemException
import org.sandboxpowered.silica.data.jackson.jsonMapperForReading
import org.sandboxpowered.silica.resources.ResourceManager

class RecipeReader : DataReader(
    category = "recipes",
    logLoadingErrors = false
) {

    override val om = jsonMapperForReading(
        SimpleModule()
            .addDeserializer(Identifier::class, IdentifierDeserializer)
            .addDeserializer(ItemStack::class, ItemStackDeserializer)
    ) {
        setDefaultTyping(RecipeTypeResolverBuilder)
        registerSubtypes(Ingredient.Item::class.java, Ingredient.Tag::class.java)
    }

    override fun load(resourceManager: ResourceManager) {
        var errors = 0
        val unhandledTypes = mutableSetOf<Identifier>()
        val recipes: List<Recipe> = resourceManager.listResourcesForReading()
            .filter { (_, node) ->
                val type = Identifier(node["type"].textValue())
                if (type in Registries.RECIPE_TYPES) true
                else {
                    unhandledTypes += type
                    false
                }
            }
            .mapNotNull { (id, node) ->
                (node as ObjectNode).putPOJO("identifier", id)
                try {
                    om.treeToValue<Recipe>(node)
                } catch (e: JacksonException) {
                    if (logLoadingErrors) logger.error("Failed to read $id", e)
                    ++errors
                    null
                } catch (_: UnknownItemException) {
                    null // No need to care about these for now
                }
            }
            .toList()

        logger.info("Loaded ${recipes.size} recipes. Types : ${Registries.RECIPE_TYPES.values.keys.joinToString()}")
        if (errors > 0) logger.warn("Encountered $errors recipe loading errors")
        if (unhandledTypes.isNotEmpty()) logger.warn("There are ${unhandledTypes.size} unhandled recipe types : ${unhandledTypes.joinToString()}")
        Registries.RECIPES.registerAll(recipes)
    }

    private object RecipeTypeResolverBuilder : ObjectMapper.DefaultTypeResolverBuilder(
        ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
        BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType("org.sandboxpowered.silica.api.recipe.").build()
    ) {

        private val delegates: MutableMap<Class<*>, TypeResolverBuilder<*>> = Reference2ObjectOpenHashMap()

        private inline fun <reified T : Any?> builder(body: ObjectMapper.DefaultTypeResolverBuilder.() -> Unit) {
            delegates[T::class.java] = ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
                BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(T::class.java).build()
            ).also(body)
        }

        override fun useForType(t: JavaType) = t.rawClass in delegates

        init {
            builder<Recipe> {
                init(JsonTypeInfo.Id.NAME, RecipeTypeIdResolver(TypeFactory.defaultInstance()))
                inclusion(JsonTypeInfo.As.EXISTING_PROPERTY)
                typeProperty(Recipe::type.name)
            }
            builder<Ingredient> {
                init(JsonTypeInfo.Id.DEDUCTION, null)
            }
        }

        override fun buildTypeSerializer(
            config: SerializationConfig,
            baseType: JavaType,
            subtypes: Collection<NamedType>?
        ): TypeSerializer? {
            val delegate = delegates[baseType.rawClass]
            return if (delegate != null) delegate.buildTypeSerializer(config, baseType, subtypes)
            else super.buildTypeSerializer(config, baseType, subtypes)
        }

        override fun buildTypeDeserializer(
            config: DeserializationConfig,
            baseType: JavaType,
            subtypes: Collection<NamedType>?
        ): TypeDeserializer? {
            val delegate = delegates[baseType.rawClass]
            return if (delegate != null) delegate.buildTypeDeserializer(config, baseType, subtypes)
            else super.buildTypeDeserializer(config, baseType, subtypes)
        }

        private class RecipeTypeIdResolver(typeFactory: TypeFactory) : TypeIdResolverBase(
            typeFactory.constructType(Recipe::class.java), typeFactory,
        ) {
            override fun idFromValue(value: Any): String {
                require(value is Recipe) { "Tried to resolve id from value of type ${value.javaClass}" }
                return value.type.toString()
            }

            override fun idFromValueAndType(value: Any, suggestedType: Class<*>) = idFromValue(value)

            override fun getMechanism(): JsonTypeInfo.Id = JsonTypeInfo.Id.NAME

            override fun typeFromId(context: DatabindContext, id: String): JavaType =
                Registries.RECIPE_TYPES[Identifier(id)].orNull()?.let { _typeFactory.constructType(it.recipeClass) }
                    ?: error("Unknown recipe type: $id")
        }
    }
}
