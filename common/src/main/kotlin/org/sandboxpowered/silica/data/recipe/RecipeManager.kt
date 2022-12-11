package org.sandboxpowered.silica.data.recipe

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.*
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.*
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.data.jackson.IdentifierDeserializer
import org.sandboxpowered.silica.data.jackson.IdentifierSerializer
import org.sandboxpowered.silica.data.jackson.ItemStackDeserializer
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.utilities.Identifier

class RecipeManager {

    private val om = jsonMapper {
        addModules(
            kotlinModule {
                enable(KotlinFeature.NullToEmptyCollection)
                enable(KotlinFeature.NullToEmptyMap)
            },
            BlackbirdModule(),
            SimpleModule()
                .addSerializer(Identifier::class.java, IdentifierSerializer)
                .addDeserializer(Identifier::class, IdentifierDeserializer)
                .addDeserializer(ItemStack::class, ItemStackDeserializer)
        )
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        defaultPrettyPrinter(DefaultPrettyPrinter())
        setDefaultTyping(RecipeTypeResolverBuilder)
        registerSubtypes(Ingredient.Item::class.java, Ingredient.Tag::class.java)
    }

    private val logger = getLogger()
    private val logRegipeErrors = false

    fun load(resourceManager: ResourceManager) {
        var errors = 0
        val recipes: List<Recipe> = resourceManager.listResources(category = "recipes") { it.endsWith(".json") }
            .asSequence()
            .map {
//            resourceManager.open(it).use(om::readValue)
                it to resourceManager.open(it).use(om::readTree)
            }
            .filter { (_, node) -> Identifier(node["type"].textValue()) in Registries.RECIPE_TYPES }
            .mapNotNull { (id, node) ->
                (node as ObjectNode).put("identifier", om.writeValueAsString(id))
                try {
                    om.treeToValue<Recipe>(node)
                } catch (e: JacksonException) {
                    if (logRegipeErrors) logger.error("Failed to read $id", e)
                    else ++errors
                    null
                }
            }.toList()

        logger.info("Loading ${recipes.size} recipes. Types : ${Registries.RECIPE_TYPES.values.keys.joinToString()}")
        logger.warn("Suppressed $errors recipe loading errors")
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
