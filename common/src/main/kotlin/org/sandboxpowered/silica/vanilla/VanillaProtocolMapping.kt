package org.sandboxpowered.silica.vanilla

import com.google.gson.Gson
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.fromJson
import org.sandboxpowered.silica.util.extensions.getResourceAsString

class VanillaProtocolMapping {
    private val gson = Gson()
    private val registryMap = HashMap<String, RegistryReference>()

    operator fun get(string: String): RegistryReference? = registryMap[string]

    fun load() {
        val string = javaClass.getResourceAsString("/data/silica/registries.json")
        val json = gson.fromJson<JsonObject>(string)
        json.keySet().forEach { registryKey ->
            val registryObject = json.getAsJsonObject(registryKey)
            val default = registryObject.getAsString("default")
            val registryProtocolId = registryObject.getAsInt("protocol_id")
            val registryReference = RegistryReference(default, registryProtocolId)
            val entries = registryObject.getAsJsonObject("entries")
            entries.keySet().forEach { registryEntryKey ->
                val entry = entries.getAsJsonObject(registryEntryKey)
                val protocolId = entry.getAsInt("protocol_id")
                registryReference.entries.put(registryEntryKey, protocolId)
            }
            registryMap.put(registryKey, registryReference)
        }
    }

    class RegistryReference(val default: String?, val protocol: Int) {
        operator fun get(identifier: Identifier): Int {
            return entries.getInt(identifier.toString())
        }

        val entries: Object2IntMap<String> = Object2IntOpenHashMap()

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
