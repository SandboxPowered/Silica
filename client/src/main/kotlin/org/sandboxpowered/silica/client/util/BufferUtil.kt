package org.sandboxpowered.silica.client.util

import org.lwjgl.system.MemoryStack
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

fun streamToBuffer(stack: MemoryStack, stream: InputStream, bufferSize: Int): ByteBuffer {
    var buffer: ByteBuffer
    if (stream is FileInputStream) {
        stream.channel.use { fc ->
            buffer = stack.malloc(fc.size().toInt() + 1)
            while (fc.read(buffer) != -1) {
            }
        }
    } else {
        Channels.newChannel(stream).use { rbc ->
            buffer = stack.malloc(bufferSize)
            while (true) {
                val bytes = rbc.read(buffer)
                if (bytes == -1) {
                    break
                }
                if (buffer.remaining() == 0) {
                    buffer = resizeBuffer(stack, buffer, buffer.capacity() * 3 / 2) // 50%
                }
            }
        }
    }
    buffer.flip()
    return buffer
}

private fun resizeBuffer(stack: MemoryStack, buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
    val newBuffer = stack.malloc(newCapacity)
    buffer.flip()
    newBuffer.put(buffer)
    return newBuffer
}