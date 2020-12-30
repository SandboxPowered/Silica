package org.sandboxpowered.silica.client.util;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class Sizes {
    private static final Map<Class<?>, Integer> SIZEOF_CACHE = new HashMap<>();

    static {
        SIZEOF_CACHE.put(Byte.class, Byte.BYTES);
        SIZEOF_CACHE.put(Character.class, Character.BYTES);
        SIZEOF_CACHE.put(Short.class, Short.BYTES);
        SIZEOF_CACHE.put(Integer.class, Integer.BYTES);
        SIZEOF_CACHE.put(Float.class, Float.BYTES);
        SIZEOF_CACHE.put(Long.class, Long.BYTES);
        SIZEOF_CACHE.put(Double.class, Double.BYTES);

        SIZEOF_CACHE.put(Vector2f.class, 2 * Float.BYTES);
        SIZEOF_CACHE.put(Vector3f.class, 3 * Float.BYTES);
        SIZEOF_CACHE.put(Vector4f.class, 4 * Float.BYTES);

        SIZEOF_CACHE.put(Matrix4f.class, SIZEOF_CACHE.get(Vector4f.class));
    }

    public static int sizeOf(Object obj) {
        return obj == null ? 0 : SIZEOF_CACHE.getOrDefault(obj.getClass(), 0);
    }

    public static int alignOf(Object obj) {
        return obj == null ? 0 : SIZEOF_CACHE.getOrDefault(obj.getClass(), Integer.BYTES);
    }

    public static int alignAs(int offset, int alignment) {
        return offset % alignment == 0 ? offset : ((offset - 1) | (alignment - 1)) + 1;
    }
}
