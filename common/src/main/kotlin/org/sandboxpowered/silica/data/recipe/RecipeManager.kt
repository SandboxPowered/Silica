package org.sandboxpowered.silica.data.recipe

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.*
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.data.jackson.IdentifierDeserializer
import org.sandboxpowered.silica.data.jackson.IdentifierSerializer
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.utilities.Identifier
import java.io.File

class RecipeManager {

    private val om = jsonMapper {
        addModules(
            kotlinModule {
                enable(KotlinFeature.NullToEmptyCollection)
                enable(KotlinFeature.NullToEmptyMap)
            },
            BlackbirdModule(),
            SimpleModule().apply {
                addDeserializer(Identifier::class.java, IdentifierDeserializer)
                addSerializer(Identifier::class.java, IdentifierSerializer)

            }
        )
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        defaultPrettyPrinter(DefaultPrettyPrinter())
        setDefaultTyping(RecipeTypeResolverBuilder)
        registerSubtypes(Ingredient.Item::class.java, Ingredient.Tag::class.java)
    }

    private val logger = getLogger()

    fun load(resourceManager: ResourceManager) {
        val recipes: List<Recipe> = resourceManager.listResources(category = "recipes") { it.endsWith(".json") }
            .asSequence()
            .map {
//            resourceManager.open(it).use(om::readValue)
                resourceManager.open(it).use(om::readTree)
            }
            .filter { it["type"].textValue() == "minecraft:smelting" }.map {
                om.treeToValue<Recipe>(it)
            }.toList()

        logger.info("Loading ${recipes.size} recipes. Types : ${recipes.groupBy { it.type }.keys.joinToString()}")
        File("allRecipes.json").outputStream().use {
            om.writeValue(it, recipes)
        }
    }

    private object RecipeTypeResolverBuilder : ObjectMapper.DefaultTypeResolverBuilder(
        ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
        BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType("org.sandboxpowered.silica.api.recipe").build()
    ) {

        private val delegates: MutableMap<Class<*>, TypeResolverBuilder<*>> = Reference2ObjectOpenHashMap()

        private inline fun <reified T : Any?> builder(body: ObjectMapper.DefaultTypeResolverBuilder.() -> Unit) {
            delegates[T::class.java] = ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(T::class.java).build()
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