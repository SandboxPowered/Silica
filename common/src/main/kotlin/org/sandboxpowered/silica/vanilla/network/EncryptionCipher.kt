package org.sandboxpowered.silica.vanilla.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import javax.crypto.Cipher

data class EncryptionCipher(val cipher: Cipher) {
    private var intermediaryRead = ByteArray(0)
    private var intermediaryWrite = ByteArray(0)

    private fun convertToArray(byteBuf: ByteBuf): ByteArray {
        val i = byteBuf.readableBytes()
        intermediaryRead = intermediaryRead.grow(i)
        byteBuf.readBytes(this.intermediaryRead, 0, i)
        return this.intermediaryRead
    }

    fun decrypt(ctx: ChannelHandlerContext, msg: ByteBuf): ByteBuf {
        val readable = msg.readableBytes()
        val array = convertToArray(msg)
        val heap = ctx.alloc().heapBuffer(cipher.getOutputSize(readable))
        heap.writerIndex(cipher.update(array, 0, readable, heap.array(), heap.arrayOffset()))
        return heap
    }

    fun encrypt(input: ByteBuf, output: ByteBuf) {
        val readable = input.readableBytes()
        val array = convertToArray(input)
        intermediaryWrite = intermediaryWrite.grow(cipher.getOutputSize(readable))
        output.writeBytes(intermediaryWrite, 0, cipher.update(array, 0, readable, intermediaryWrite))
    }

}

private fun ByteArray.grow(i: Int): ByteArray = if (size < i) ByteArray(i) else this
