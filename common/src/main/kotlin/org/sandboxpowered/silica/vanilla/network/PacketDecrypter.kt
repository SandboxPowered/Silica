package org.sandboxpowered.silica.vanilla.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import javax.crypto.Cipher

class PacketDecrypter private constructor(private val cipher: EncryptionCipher) : MessageToMessageDecoder<ByteBuf>() {
    companion object {
        operator fun invoke(cipher: Cipher): PacketDecrypter = PacketDecrypter(EncryptionCipher(cipher))
    }

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        out += cipher.decrypt(ctx, msg)
    }
}