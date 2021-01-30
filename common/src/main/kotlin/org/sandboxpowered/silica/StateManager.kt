package org.sandboxpowered.silica

import com.google.gson.Gson
import com.google.gson.JsonArray
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.property.Property
import org.sandboxpowered.silica.block.SilicaBlockState
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.getResourceAsString
import java.lang.StringBuilder

class StateManager {
    val rawMap: MutableMap<String, Object2IntMap<String>> = LinkedHashMap()
    val stateMap: Object2IntMap<BlockState> = Object2IntArrayMap()
    val idMap: Int2ObjectMap<BlockState> = Int2ObjectArrayMap()

    fun load(): Boolean {
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

        var encountedAnyProblem: Boolean = false

        SilicaRegistries.BLOCK_REGISTRY.internalMap.forEach { (id, block) ->
            val blockMap = rawMap.get(id.toString())
            if(blockMap==null) {
                println("Vanilla seems to not know this block $id")
                encountedAnyProblem = true
            } else {
                block.stateFactory.validStates.map { it as SilicaBlockState }.forEach { state ->
                    val builder = StringBuilder()
                    state.properties.forEach { entry ->
                        val value = entry.value as Comparable<Any>
                        val prop = entry.key as Property<Comparable<Any>>
                        if (builder.isNotEmpty()) builder.append(",")
                        builder.append(prop.name).append("@").append(prop.getName(value))
                    }

                    val intId = blockMap.getInt(builder.toString())

                    if (intId == -1) {
                        println("Vanilla seems to not know this state $id[$builder]")
                        encountedAnyProblem = true
                    } else {
                        stateMap[state] = intId
                        idMap[intId] = state
                    }
                }
            }
        }
        return encountedAnyProblem
    }

    fun toVanillaId(state: BlockState): Int = stateMap.getInt(state)
    fun fromVanillaId(id: Int): BlockState = idMap.getOrElse(id) { idMap.get(0) }
}

private inline fun <reified T> Gson.fromJson(s: String): T {
    return fromJson(s, T::class.java)
}
