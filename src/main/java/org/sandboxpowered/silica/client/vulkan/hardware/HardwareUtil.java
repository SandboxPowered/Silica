package org.sandboxpowered.silica.client.vulkan.hardware;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.sandboxpowered.silica.client.Silica;
import org.sandboxpowered.silica.client.vulkan.error.VkError;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.sandboxpowered.silica.client.Silica.ENABLE_DEBUG;
import static org.sandboxpowered.silica.client.vulkan.instance.InstanceUtil.VALIDATION_LAYERS;
import static org.sandboxpowered.silica.client.util.BufferUtil.asPointerBuffer;

public class HardwareUtil {
    private static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME).collect(toSet());

    public static VkPhysicalDevice[] getPhysicalDevices(VkInstance instance) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer devicesCount = stack.mallocInt(1);
            int result = vkEnumeratePhysicalDevices(instance, devicesCount, null);
            VkError.validate(result, "Could not enumerate physical devices!");

            int devCount = devicesCount.get(0);
            PointerBuffer pDevices = stack.mallocPointer(devCount);

            result = vkEnumeratePhysicalDevices(instance, devicesCount, pDevices);
            VkError.validate(result, "Could not enumerate physical devices!");

            VkPhysicalDevice[] devices = new VkPhysicalDevice[devCount];
            for (int i = 0; i < devCount; i++) {
                devices[i] = new VkPhysicalDevice(pDevices.get(i), instance);
            }
            return devices;
        }
    }

    public static VkPhysicalDevice findOptimalPhysicalDevice(VkPhysicalDevice[] physicalDevices, long surface) {
        VkPhysicalDevice selectedDevice = null;
        int selectedDeviceScore = -1;
        try (MemoryStack stack = stackPush()) {
            for (VkPhysicalDevice device : physicalDevices) {
                if (!isDeviceSuitable(device, surface))
                    continue;

                int deviceScore = rateDevice(device);
                VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.callocStack(stack);
                vkGetPhysicalDeviceProperties(device, properties);

                Silica.LOG.debug("Considering " + properties.deviceNameString() + " with a score of " + deviceScore);
                if (selectedDeviceScore < deviceScore) {
                    selectedDevice = device;
                    selectedDeviceScore = deviceScore;
                }
            }
        }
        return selectedDevice;
    }

    private static int rateDevice(VkPhysicalDevice device) {
        int score = 0;
        try (MemoryStack stack = stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.callocStack(stack);
            vkGetPhysicalDeviceProperties(device, properties);

            switch (properties.deviceType()) {
                case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> score += 1000;
                case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> score -= 500;
            }

            IntBuffer extensionCount = stack.ints(0);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null);
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions);
            Set<String> extensions = availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet());

            if (extensions.contains("VK_NV_ray_tracing"))
                score += 1000;

            score += properties.limits().maxImageDimension2D();
        }
        return score;
    }

    private static boolean isDeviceSuitable(VkPhysicalDevice device, long surface) {
        QueueFamilyIndices indices = findQueueFamilies(device, surface);
        boolean extensionsSupported = checkDeviceExtensionSupport(device);
        boolean swapChainAdequate = false;
        boolean anisotropySupported = false;

        if (extensionsSupported) {
            try (MemoryStack stack = stackPush()) {
                SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device, surface, stack);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();
                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
                vkGetPhysicalDeviceFeatures(device, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return indices.isComplete() && extensionsSupported && swapChainAdequate && anisotropySupported;
    }

    private static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, long surface, MemoryStack stack) {
        SwapChainSupportDetails details = new SwapChainSupportDetails();
        details.capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);
        IntBuffer count = stack.ints(0);
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);
        if (count.get(0) != 0) {
            details.formats = VkSurfaceFormatKHR.mallocStack(count.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats);
        }
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);
        if (count.get(0) != 0) {
            details.presentModes = stack.mallocInt(count.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes);
        }
        return details;
    }

    private static boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer extensionCount = stack.ints(0);
            VkError.validate(vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null), "Could not find device extension count");
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);
            VkError.validate(vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions), "Could not enumerate device extensions");
            return availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet()).containsAll(DEVICE_EXTENSIONS);
        }
    }

    private static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface) {
        QueueFamilyIndices indices = new QueueFamilyIndices();
        try (MemoryStack stack = stackPush()) {
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);
            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for (int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++) {
                if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }
                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);
                if (presentSupport.get(0) == VK_TRUE) {
                    indices.presentFamily = i;
                }
            }
        }
        return indices;
    }

    public static LogicalDevice createLogicalDevice(VkPhysicalDevice physicalDevice, long surface) {
        try (MemoryStack stack = stackPush()) {
            QueueFamilyIndices indices = findQueueFamilies(physicalDevice, surface);
            int[] uniqueQueueFamilies = indices.unique();
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilies.length, stack);

            for (int i = 0; i < uniqueQueueFamilies.length; i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
            }

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack);
            deviceFeatures.samplerAnisotropy(true);
            deviceFeatures.sampleRateShading(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            createInfo.pEnabledFeatures(deviceFeatures);
            createInfo.ppEnabledExtensionNames(asPointerBuffer(DEVICE_EXTENSIONS));

            if (ENABLE_DEBUG) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
            }

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

            int res = vkCreateDevice(physicalDevice, createInfo, null, pDevice);
            VkError.validate(res, "Failed to create logical device");

            VkDevice device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);
            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(device, indices.graphicsFamily, 0, pQueue);
            VkQueue graphicsQueue = new VkQueue(pQueue.get(0), device);
            vkGetDeviceQueue(device, indices.presentFamily, 0, pQueue);
            VkQueue presentQueue = new VkQueue(pQueue.get(0), device);

            return new LogicalDevice(device, graphicsQueue, presentQueue);
        }
    }

    public static long createCommandPool(VkPhysicalDevice physicalDevice, LogicalDevice device, long surface) {
        try (MemoryStack stack = stackPush()) {
            QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice, surface);
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);
            LongBuffer pCommandPool = stack.mallocLong(1);
            int res = vkCreateCommandPool(device.getLogicalDevice(), poolInfo, null, pCommandPool);
            VkError.validate(res, "Failed to create command pool");
            return pCommandPool.get(0);
        }
    }
}