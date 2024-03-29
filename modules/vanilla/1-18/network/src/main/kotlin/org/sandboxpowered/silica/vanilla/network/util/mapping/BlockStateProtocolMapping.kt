package org.sandboxpowered.silica.vanilla.network.util.mapping

import com.google.gson.Gson
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.extensions.fromJson
import org.sandboxpowered.silica.api.util.extensions.getNullable
import org.sandboxpowered.silica.api.util.extensions.getResourceAsString
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.persistence.BlockStateMapping
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.state.property.Property
import java.util.function.Function
import kotlin.system.measureTimeMillis

class BlockStateProtocolMapping private constructor() : BlockStateMapping {
    companion object {
        val INSTANCE by lazy { BlockStateProtocolMapping() }
    }

    private val rawMap = Object2ObjectOpenHashMap<String, Object2IntMap<String>>()
    private val stateMap = Object2IntOpenHashMap<BlockState>()
    private val idMap = Int2ObjectOpenHashMap<BlockState>()

    private val logger = getLogger()

    fun load(): Map<MappingErrorType, Set<String>> {
        val errorMap = Object2ObjectOpenHashMap<MappingErrorType, ObjectOpenHashSet<String>>()
        measureTimeMillis {
            val string = javaClass.getResourceAsString("/data/minecraft/blocks.json")
            val gson = Gson()
            val json = gson.fromJson<JsonObject>(string)
            json.keySet().forEach { key ->
                val obj = json[key].asJsonObject
                val statesArray = obj.getAsJsonArray("states")
                val m = rawMap.computeIfAbsent(key, Function {
                    Object2IntOpenHashMap<String>().apply { defaultReturnValue(-1) }
                })
                statesArray.map { it.asJsonObject }.forEach {
                    val id = it.get("id").asInt
                    val properties = it.getNullable("properties")?.asJsonObject
                    val stateString = buildString {
                        properties?.keySet()?.forEach { prop ->
                            if (isNotEmpty())
                                append(',')
                            append(prop)
                            append('@')
                            append(properties.get(prop).asString)
                        }
                    }
                    m.put(stateString, id)
                }
            }
//            json.forEach {
//                val obj = it.asJsonObject
//                val id = obj["id"].asString
//                val states = obj["states"].asJsonObject
//                val m = rawMap.computeIfAbsent(id) {
//                    Object2IntOpenHashMap<String>().apply { defaultReturnValue(-1) }
//                }
//                states.entrySet().map { entry ->
//                    m.put(entry.key, entry.value.asInt)
//                }
//            }

            val unknown = ObjectOpenHashSet<String>()

            Registries.BLOCKS.values.forEach { (id, block) ->
                val blockMap = rawMap[id.toString()]
                if (blockMap == null) unknown.add(id.toString())
                else block.stateProvider.validStates.map { it }.forEach { state ->
                    val builder = StringBuilder()
                    state.properties.forEach { entry ->
                        val value = entry.value as Comparable<Any>
                        val prop = entry.key as Property<Comparable<Any>>
                        if (builder.isNotEmpty()) builder.append(",")
                        builder.append(prop.propertyName).append("@").append(prop.getValueString(value))
                    }

                    val stateString = builder.toString()

                    val intId = blockMap.getInt(stateString)

                    if (intId == -1) unknown.add("$id[$builder]")
                    else {
                        stateMap[state] = intId
                        idMap[intId] = state
                        blockMap.remove(stateString)
                        if (blockMap.isEmpty())
                            rawMap.remove(id.toString())
                    }
                }
            }

            val missing = ObjectOpenHashSet<String>()

            rawMap.forEach { (block, u) ->
                u.forEach { (state, _) ->
                    if (state.isEmpty()) missing.add(block)
                    else missing.add("$block[$state]")
                }
            }
            rawMap.clear()
            if (unknown.isNotEmpty()) errorMap[MappingErrorType.UNKNOWN] = unknown
            if (missing.isNotEmpty()) errorMap[MappingErrorType.MISSING] = missing
        }.let { logger.debug("Took ${it}ms to collect vanilla state mappings") }
        return errorMap
    }

    override operator fun get(state: BlockState): Int = stateMap.getInt(state)
    override operator fun get(id: Int): BlockState = idMap.getOrElse(id) { idMap.get(0) }

}
