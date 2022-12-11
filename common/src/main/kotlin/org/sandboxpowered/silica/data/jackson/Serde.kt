package org.sandboxpowered.silica.data.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.utilities.Identifier

internal object IdentifierDeserializer : FromStringDeserializer<Identifier>(Identifier::class.java) {
    override fun _deserialize(value: String, ctxt: DeserializationContext) = Identifier(value)
}

internal val IdentifierSerializer: ToStringSerializer = ToStringSerializer.instance

internal object ItemStackDeserializer : StdDeserializer<ItemStack>(ItemStack::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemStack {
        return when (p.currentToken) {
            JsonToken.START_OBJECT -> deserializeFromObject(p, ctxt)
            JsonToken.VALUE_STRING -> deserializeFromString(p, ctxt)
            else -> {
                ctxt.reportWrongTokenException(this, p.currentToken, "Could not read ItemStack")
                error("Unreachable")
            }
        }
    }

    private fun deserializeFromObject(p: JsonParser, ctxt: DeserializationContext): ItemStack {
        val tree = ctxt.readTree(p)
        val item = Registries.ITEMS[ctxt.readTreeAsValue(tree["item"], Identifier::class.java)]
        if (!item.isPresent) throw UnknownItemException("Unknown item ${item.id} !")
        val count = tree["count"]
        return if (count == null) ItemStack(item)
        else ItemStack(item, count.intValue())
    }

    private fun deserializeFromString(p: JsonParser, ctxt: DeserializationContext): ItemStack {
        return ItemStack(Registries.ITEMS[ctxt.readValue(p, Identifier::class.java)])
    }
}

class UnknownItemException(message: String?) : RuntimeException(message)
