package org.sandboxpowered.silica

import com.google.gson.Gson
import com.google.gson.JsonArray
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.property.Property
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.state.block.SilicaBlockState
import org.sandboxpowered.silica.util.getResourceAsString

class StateManager {
    private val rawMap: MutableMap<String, Object2IntMap<String>> = LinkedHashMap()
    private val stateMap: Object2IntMap<BlockState> = Object2IntArrayMap()
    private val idMap: Int2ObjectMap<BlockState> = Int2ObjectArrayMap()

    fun load(): Pair<List<String>, List<String>> {
        val string = javaClass.getResourceAsString("/data/silica/states.json")
        val gson = Gson()
        val json = gson.fromJson<JsonArray>(string)
        json.forEach {
            val obj = it.asJsonObject
            val id = obj["id"].asString
            val states = obj["states"].asJsonObject
            val m = rawMap.computeIfAbsent(id) {
                val map = Object2IntArrayMap<String>()
                map.defaultReturnValue(-1)
                map
            }
            states.entrySet().map { entry ->
                m.put(entry.key, entry.value.asInt)
            }
        }

        val errors = ArrayList<String>()

        SilicaRegistries.BLOCK_REGISTRY.internalMap.forEach { (id, block) ->
            val blockMap = rawMap[id.toString()]
            if (blockMap == null) {
                errors.add(id.toString())
            } else {
                block.stateFactory.validStates.map { it as SilicaBlockState }.forEach { state ->
                    val builder = StringBuilder()
                    state.properties.forEach { entry ->
                        val value = entry.value as Comparable<Any>
                        val prop = entry.key as Property<Comparable<Any>>
                        if (builder.isNotEmpty()) builder.append(",")
                        builder.append(prop.name).append("@").append(prop.getName(value))
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

        val missing = ArrayList<String>()

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

        return errors to missing
    }

    fun toVanillaId(state: BlockState): Int = stateMap.getInt(state)
    fun fromVanillaId(id: Int): BlockState = idMap.getOrElse(id) { idMap.get(0) }
}

private inline fun <reified T> Gson.fromJson(s: String): T {
    return fromJson(s, T::class.java)
}
