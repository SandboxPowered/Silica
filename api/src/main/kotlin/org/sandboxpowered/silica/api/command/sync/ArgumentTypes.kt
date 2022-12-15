package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.network.PacketBuffer
import kotlin.experimental.or

object ArgumentTypes {
    private val BY_CLASS: MutableMap<Class<*>, Entry<*>> = HashMap()
    private val BY_NAME: MutableMap<Identifier, Entry<*>> = HashMap()

    inline fun <reified T : ArgumentType<*>> register(string: String, argumentSerializer: ArgumentSerializer<T>) {
        register(string, T::class.java, argumentSerializer)
    }

    fun <T : ArgumentType<*>> register(string: String, clazz: Class<T>, argumentSerializer: ArgumentSerializer<T>) {
        val resourceLocation = Identifier(string)
        require(!BY_CLASS.containsKey(clazz)) { "Class ${clazz.name} already has a serializer!" }
        require(!BY_NAME.containsKey(resourceLocation)) { "'$resourceLocation' is already a registered serializer!" }
        val entry = Entry(clazz, argumentSerializer, resourceLocation)
        BY_CLASS[clazz] = entry
        BY_NAME[resourceLocation] = entry
    }

    init {
        register("brigadier:bool", SingletonArgumentSerializer { BoolArgumentType.bool() })
        register("brigadier:float", FloatArgumentSerializer())
        register("brigadier:double", DoubleArgumentSerializer())
        register("brigadier:integer", IntArgumentSerializer())
        register("brigadier:long", LongArgumentSerializer())
        register("brigadier:string", StringArgumentSerializer())
    }

    data class Entry<T : ArgumentType<*>>(
        val clazz: Class<T>,
        val serializer: ArgumentSerializer<T>,
        val name: Identifier
    )

    interface ArgumentSerializer<T : ArgumentType<*>> {
        fun serializeToNetwork(argumentType: T, buffer: PacketBuffer)
        fun deserializeFromNetwork(buffer: PacketBuffer): T
        fun serializeToJson(argumentType: T, jsonObject: JsonObject)
    }


    fun createNumberFlags(bl: Boolean, bl2: Boolean): Byte {
        var b: Byte = 0
        if (bl) b = (b or 1)
        if (bl2) b = (b or 2)
        return b
    }

    fun <T : ArgumentType<*>> serialize(buffer: PacketBuffer, type: T) {
        val entry = BY_CLASS[type.javaClass] as? Entry<T>
        if (entry == null) {
            buffer.writeIdentifier(Identifier(""))
        } else {
            buffer.writeIdentifier(entry.name)
            entry.serializer.serializeToNetwork(type, buffer)
        }
    }
}