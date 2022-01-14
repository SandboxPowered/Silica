package org.sandboxpowered.silica.client.opengl

import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.client.opengl.RenderingFormat.Attribute

object RenderingFormats {
    val POSITION_TEXTURE = RenderingFormat(
        Identifier("silica", "main"),
        Attribute("position", 3, RenderingFormat.DataType.FLOAT, false),
        Attribute("texCoord", 2, RenderingFormat.DataType.FLOAT, false),
        Attribute("normal", 3, RenderingFormat.DataType.FLOAT, false),
        Attribute("tangent", 3, RenderingFormat.DataType.FLOAT, false),
        Attribute("bitangent", 3, RenderingFormat.DataType.FLOAT, false)
    )
}