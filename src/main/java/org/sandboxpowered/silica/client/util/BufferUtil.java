package org.sandboxpowered.silica.client.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.util.Collection;

import static org.lwjgl.system.MemoryStack.stackGet;

public class BufferUtil {
    public static PointerBuffer asPointerBuffer(Collection<String> collection) {
        MemoryStack stack = stackGet();
        PointerBuffer buffer = stack.mallocPointer(collection.size());
        collection.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);
        return buffer.rewind();
    }
}
