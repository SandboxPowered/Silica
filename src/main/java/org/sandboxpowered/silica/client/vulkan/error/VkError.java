package org.sandboxpowered.silica.client.vulkan.error;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VkError {
    public static void validate(int res, String message) {
        if (res == VK_SUCCESS)
            return;
        throw new RuntimeException(message);
    }
}