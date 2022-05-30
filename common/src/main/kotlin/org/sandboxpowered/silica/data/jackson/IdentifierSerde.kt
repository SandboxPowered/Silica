package org.sandboxpowered.silica.data.jackson

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.sandboxpowered.utilities.Identifier

internal object IdentifierDeserializer : FromStringDeserializer<Identifier>(Identifier::class.java) {
    override fun _deserialize(value: String, ctxt: DeserializationContext) = Identifier(value)
}

internal val IdentifierSerializer: ToStringSerializer = ToStringSerializer.instance
