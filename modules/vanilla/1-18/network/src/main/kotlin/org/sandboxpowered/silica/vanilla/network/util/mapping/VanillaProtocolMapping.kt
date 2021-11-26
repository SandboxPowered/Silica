package org.sandboxpowered.silica.vanilla.network.util.mapping

import com.google.gson.Gson
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.fromJson
import org.sandboxpowered.silica.api.util.extensions.getResourceAsString

class VanillaProtocolMapping private constructor() {
    companion object {
        val INSTANCE = VanillaProtocolMapping()
    }

    private val gson = Gson()
    private val registryMap = HashMap<String, RegistryReference>()

    operator fun get(string: String): RegistryReference {
        return registryMap[string] ?: error("Unable to find $string registry")
    }

    fun load(): Map<String, Map<MappingErrorType, Set<String>>> {
        val string = javaClass.getResourceAsString("/data/minecraft/registries.json")
        val json = gson.fromJson<JsonObject>(string)
        val map: Object2ObjectMap<String, ObjectList<String>> = Object2ObjectOpenHashMap()
        json.entrySet().forEach { (registryKey, registryObject) ->
            if (registryObject is JsonObject) {
                val default = registryObject.getAsString("default")
                val registryProtocolId = registryObject.getAsInt("protocol_id")
                val registryReference = RegistryReference(registryKey, default, registryProtocolId)
                val entries = registryObject.getAsJsonObject("entries")
                val list = map.computeIfAbsent(registryKey) {
                    ObjectArrayList()
                }
                entries.keySet().forEach { registryEntryKey ->
                    val entry = entries.getAsJsonObject(registryEntryKey)
                    val protocolId = entry.getAsInt("protocol_id")
                    registryReference[registryEntryKey] = protocolId
                    list.add(registryEntryKey)
                }
                registryMap[registryKey] = registryReference
            }
        }

        map["minecraft:block"]?.apply {
            removeIf {
                Registries.BLOCKS[Identifier(it)].isPresent
            }
        }
        map["minecraft:item"]?.apply {
            removeIf {
                Registries.ITEMS[Identifier(it)].isPresent
            }
        }

        val retMap = HashMap<String, HashMap<MappingErrorType, HashSet<String>>>()

        map.forEach { (t, u) ->
            val errorMap = HashMap<MappingErrorType, HashSet<String>>()

            u.forEach {
                val registry = registryMap[t]
                val entry = registry?.get(Identifier(it))
                if (entry == null) {
                    errorMap.computeIfAbsent(MappingErrorType.UNKNOWN) {
                        HashSet()
                    }.add(it)
                } else {
                    errorMap.computeIfAbsent(MappingErrorType.MISSING) {
                        HashSet()
                    }.add(it)
                }
            }

            retMap[t] = errorMap
        }

        return retMap
    }

    class RegistryReference(val registryKey: String, val default: String?, val protocol: Int) {
        operator fun get(identifier: Identifier): Int {
            return entries.getInt(identifier.toString())
        }

        operator fun get(id: Int): Identifier = (reverseEntries[id] ?: default)?.let(Identifier::invoke)
            ?: error("Id $id not found in registry $registryKey without a default")

        operator fun set(identifier: String, value: Int) {
            entries[identifier] = value
            reverseEntries[value] = identifier
        }

        private val entries: Object2IntMap<String> = Object2IntOpenHashMap()
        private val reverseEntries: Int2ObjectMap<String> = Int2ObjectOpenHashMap()

        init {
            entries.defaultReturnValue(0)
        }
    }
}

private fun JsonObject.getAsString(memberName: String): String? {
    if (!has(memberName))
        return null
    return get(memberName).asString
}

private fun JsonObject.getAsInt(memberName: String): Int {
    return get(memberName).asInt
}
