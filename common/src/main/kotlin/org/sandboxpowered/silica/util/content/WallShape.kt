package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class WallShape(private val s: String) : StringSerializable{
    NONE("none"),
    LOW("low"),
    TALL("tall");

    override fun getName(): String = s
}