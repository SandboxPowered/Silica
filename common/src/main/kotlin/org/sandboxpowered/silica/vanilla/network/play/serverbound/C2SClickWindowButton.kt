package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

/**
 * @param window the id of the window sent by Open Window
 * @param button per window :
 *      - enchantment table : 0 = topmost, 1 = middle, 2 = bottom
 *      - lectern : 1 = previous page, 2 = next page, 3 = take book, > 100 = open page (100 + pageNumber)
 *      - stone cutter : recipe button number - 4 * row + col. Depends on the item (?)
 *      - loom : recipe button number - 4 * row + col. Depends on the item (?)
 */
data class C2SClickWindowButton(private val window: Byte, private val button: Byte) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readByte(), buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeByte(window)
        buf.writeByte(button)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle click window button
    }
}