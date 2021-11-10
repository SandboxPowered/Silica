package org.sandboxpowered.silica.vanilla

import com.google.gson.Gson
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.util.extensions.fromJson
import org.sandboxpowered.silica.util.extensions.getResourceAsString
import org.sandboxpowered.silica.util.extensions.set

class VanillaProtocolMapping {
    private val gson = Gson()
    private val registryMap = HashMap<String, RegistryReference>()

    operator fun get(string: String): RegistryReference {
        return registryMap[string] ?: error("Unable to find $string registry")
    }

    fun load() {
        val string = javaClass.getResourceAsString("/data/silica/registries.json")
        val json = gson.fromJson<JsonObject>(string)
        json.entrySet().forEach { (registryKey, registryObject) ->
            if (registryObject is JsonObject) {
                val default = registryObject.getAsString("default")
                val registryProtocolId = registryObject.getAsInt("protocol_id")
                val registryReference = RegistryReference(registryKey, default, registryProtocolId)
                val entries = registryObject.getAsJsonObject("entries")
                entries.keySet().forEach { registryEntryKey ->
                    val entry = entries.getAsJsonObject(registryEntryKey)
                    val protocolId = entry.getAsInt("protocol_id")
                    registryReference[registryEntryKey] = protocolId
                }
                registryMap[registryKey] = registryReference
            }
        }
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
