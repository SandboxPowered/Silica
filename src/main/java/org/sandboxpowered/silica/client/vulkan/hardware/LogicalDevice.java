package org.sandboxpowered.silica.client.vulkan.hardware;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

public class LogicalDevice {
    private final VkDevice logicalDevice;
    private final VkQueue graphicsQueue;
    private final VkQueue presentQueue;

    public LogicalDevice(VkDevice logicalDevice, VkQueue graphicsQueue, VkQueue presentQueue) {
        this.logicalDevice = logicalDevice;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
    }

    public VkDevice getLogicalDevice() {
        return logicalDevice;
    }

    public VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public VkQueue getPresentQueue() {
        return presentQueue;
    }
}
