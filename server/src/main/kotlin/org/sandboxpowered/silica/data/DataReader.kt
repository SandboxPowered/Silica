package org.sandboxpowered.silica.data

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.resources.ResourceManager

abstract class DataReader(
    val category: String,
    protected val logLoadingErrors: Boolean
) {
    protected val logger = getLogger()
    protected abstract val om: ObjectMapper

    abstract fun load(resourceManager: ResourceManager)

    protected open fun ResourceManager.listResourcesForReading(): Sequence<Pair<Identifier, JsonNode>> =
        listResources(category = category) { it.endsWith(".json") }
            .asSequence().map { it.removePrefix("$category/").removeSuffix(".json") to open(it) }
            .map { (id, inputStream) ->
                id to inputStream.use(om::readTree)
            }
}

abstract class DataReaderWithSubCategories<T : Any>(
    category: String, logLoadingErrors: Boolean, protected val typeRef: TypeReference<T>
) : DataReader(category, logLoadingErrors) {

    protected abstract fun process(data: Map<String, List<T>>)

    override fun load(resourceManager: ResourceManager) {
        var errors = 0
        val loaded: Map<String, List<T>> = resourceManager.listResourcesForReading()
            .mapNotNull { (id, node) ->
                val tagCategory = id.path.substringBefore('/')
                (node as ObjectNode).putPOJO("identifier", id.removePrefix("$tagCategory/"))
                try {
                    tagCategory to om.readValue(om.treeAsTokens(node), typeRef)
                } catch (e: JacksonException) {
                    if (logLoadingErrors) logger.error("Failed to read $id", e)
                    ++errors
                    null
                }
            }
            .groupBy(Pair<String, *>::first, Pair<*, T>::second)

        logger.info("Loaded ${loaded.values.sumOf(List<*>::size)} $category in ${loaded.size} categories (${loaded.keys.joinToString()})")
        if (errors > 0) logger.warn("Encountered $errors $category loading errors")

        process(loaded)
    }
}
