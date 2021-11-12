package org.sandboxpowered.silica.vanilla.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import javax.crypto.Cipher

class PacketEncrypter private constructor(private val cipher: EncryptionCipher) : MessageToByteEncoder<ByteBuf>() {
    companion object {
        operator fun invoke(cipher: Cipher): PacketEncrypter = PacketEncrypter(EncryptionCipher(cipher))
    }

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        cipher.encrypt(msg, out)
    }
}