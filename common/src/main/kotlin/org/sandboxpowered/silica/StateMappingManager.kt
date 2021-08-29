package org.sandboxpowered.silica

import com.google.gson.Gson
import com.google.gson.JsonArray
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.block.Block
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.registry.SilicaRegistry
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.state.property.Property
import org.sandboxpowered.silica.util.extensions.getResourceAsString
import kotlin.system.measureTimeMillis

class StateManager {
    private val rawMap: Object2ObjectMap<String, Object2IntMap<String>> = Object2ObjectOpenHashMap()
    private val stateMap: Object2IntMap<BlockState> = Object2IntOpenHashMap()
    private val idMap: Int2ObjectMap<BlockState> = Int2ObjectOpenHashMap()

    private val logger = LogManager.getLogger()

    fun load(): Map<ErrorType, Set<String>> {
        val errorMap = Object2ObjectOpenHashMap<ErrorType, ObjectOpenHashSet<String>>()
        measureTimeMillis {
            val string = javaClass.getResourceAsString("/data/silica/states.json")
            val gson = Gson()
            val json = gson.fromJson<JsonArray>(string)
            json.forEach {
                val obj = it.asJsonObject
                val id = obj["id"].asString
                val states = obj["states"].asJsonObject
                val m = rawMap.computeIfAbsent(id) {
                    val map = Object2IntOpenHashMap<String>()
                    map.defaultReturnValue(-1)
                    map
                }
                states.entrySet().map { entry ->
                    m.put(entry.key, entry.value.asInt)
                }
            }

            val errors = ObjectOpenHashSet<String>()

            (SilicaRegistries.BLOCK_REGISTRY as SilicaRegistry<Block>).internalMap.forEach { (id, block) ->
                val blockMap = rawMap[id.toString()]
                if (blockMap == null) {
                    errors.add(id.toString())
                } else {
                    block.stateProvider.validStates.map { it }.forEach { state ->
                        val builder = StringBuilder()
                        state.properties.forEach { entry ->
                            val value = entry.value as Comparable<Any>
                            val prop = entry.key as Property<Comparable<Any>>
                            if (builder.isNotEmpty()) builder.append(",")
                            builder.append(prop.propertyName).append("@").append(prop.getValueString(value))
                        }

                        val stateString = builder.toString()

                        val intId = blockMap.getInt(stateString)

                        if (intId == -1) {
                            errors.add("$id[$builder]")
                        } else {
                            stateMap[state] = intId
                            idMap[intId] = state
                            blockMap.remove(stateString)
                            if (blockMap.isEmpty())
                                rawMap.remove(id.toString())
                        }
                    }
                }
            }

            val missing = ObjectOpenHashSet<String>()

            rawMap.forEach { (block, u) ->
                run {
                    u.forEach { (state, u) ->
                        if (state.isEmpty())
                            missing.add(block)
                        else
                            missing.add("$block[$state]")
                    }
                }
            }
            if (errors.isNotEmpty())
                errorMap[ErrorType.UNKNOWN] = errors
            if (missing.isNotEmpty())
                errorMap[ErrorType.MISSING] = missing
        }.apply {
            logger.info("Took {}ms to collect vanilla state mappings", this)
        }
        return errorMap
    }

    fun toVanillaId(state: BlockState): Int = stateMap.getInt(state)
    fun fromVanillaId(id: Int): BlockState = idMap.getOrElse(id) { idMap.get(0) }

    enum class ErrorType {
        MISSING,
        UNKNOWN
    }
}

private inline fun <reified T> Gson.fromJson(s: String): T {
    return fromJson(s, T::class.java)
}
