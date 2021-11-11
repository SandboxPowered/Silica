package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

@Suppress("ArrayInDataClass") // we don't care about equals/hashCode
data class C2SEditBook(
    private val hand: Int,
    private val entries: Collection<String>,
    private val title: String?
) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(
        buf.readVarInt(),
        buf.readCollection(MAX_PAGES) { it.readString(MAX_PAGE_LENGTH) },
        buf.readOptionalString(MAX_TITLE_LENGTH)
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(hand)
        buf.writeCollection(entries.take(MAX_PAGES)) { writeString(it.take(MAX_PAGE_LENGTH)) }
        buf.writeString(title, MAX_TITLE_LENGTH)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle edit book
    }

    private companion object {
        private const val MAX_TITLE_LENGTH = 128
        private const val MAX_PAGE_LENGTH = 8192
        private const val MAX_PAGES = 200
    }
}