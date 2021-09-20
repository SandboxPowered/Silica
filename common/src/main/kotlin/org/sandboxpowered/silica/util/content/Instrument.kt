package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Instrument(override val asString: String) : StringSerializable {
    HARP("harp"),
    BASEDRUM("basedrum"),
    SNARE("snare"),
    HAT("hat"),
    BASS("bass"),
    FLUTE("flute"),
    BELL("bell"),
    GUITAR("guitar"),
    CHIME("chime"),
    XYLOPHONE("xylophone"),
    IRON_XYLOPHONE("iron_xylophone"),
    COW_BELL("cow_bell"),
    DIDGERIDOO("didgeridoo"),
    BIT("bit"),
    BANJO("banjo"),
    PLING("pling");
}