package org.sandboxpowered.silica.client;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.sandboxpowered.silica.client.resources.DirectoryResourceLoader;
import org.sandboxpowered.silica.client.resources.ResourceManager;
import org.sandboxpowered.silica.client.resources.ZIPResourceLoader;
import org.sandboxpowered.silica.client.vulkan.error.VkError;
import org.sandboxpowered.silica.client.vulkan.hardware.LogicalDevice;
import org.sandboxpowered.silica.client.vulkan.instance.InstanceUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.*;
import static org.sandboxpowered.silica.client.vulkan.hardware.HardwareUtil.*;
import static org.sandboxpowered.silica.client.vulkan.instance.InstanceUtil.populateDebugMessengerCreateInfo;

public class Silica implements Runnable {
    public static final boolean ENABLE_DEBUG = DEBUG.get(true);
    public static final Logger LOG = LogManager.getLogger(Silica.class);

    private final VkInstance vInstance;
    private final VkPhysicalDevice[] vPhysicalDevices;
    private final VkPhysicalDevice physicalDevice;
    private final Window window;
    private final long surface;
    private final long debugMessenger;
    private final LogicalDevice logicalDevice;
    private final long commandPool;

    private final ResourceManager manager;


    public static Path asPath(URL url) {
        if (url.getProtocol().equals("file")) {
            return asFile(url).toPath();
        } else {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException var2) {
                LOG.error("Error finding path of url", var2);
                return null;
            }
        }
    }

    public static File asFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException var2) {
            LOG.error("Error finding file of url", var2);
            return null;
        }
    }

    public Silica(Args args) {
        List<String> list = new ArrayList<>();
        glfwSetErrorCallback((i, l) ->
                list.add(String.format("GLFW error during init: [0x%X]%s", i, l))
        );
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }
        for (String string : list) {
            LOG.error("GLFW error collected during initialization: {}", string);
        }

        manager = new ResourceManager();

        try {
            URL url = Silica.class.getResource("/log4j2.xml").toURI().resolve(".").toURL();
            Path path = asPath(url);
            if (path != null) {
                if (Files.isDirectory(path)) {
                    manager.add(new DirectoryResourceLoader(path.toFile()));
                } else {
                    manager.add(new ZIPResourceLoader(path.toFile()));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error loading default resources", e);
        }

        LOG.debug("Loaded namespaces: [{}]", StringUtils.join(manager.getNamespaces(), ","));

        window = new Window("Sandbox Silica", args.width, args.height);

        vInstance = InstanceUtil.createInstance();
        debugMessenger = setupDebugMessenger();
        surface = createSurface();
        vPhysicalDevices = getPhysicalDevices(vInstance);
        physicalDevice = findOptimalPhysicalDevice(vPhysicalDevices, surface);
        logicalDevice = createLogicalDevice(physicalDevice, surface);
        commandPool = createCommandPool(physicalDevice, logicalDevice, surface);

        while (!window.shouldClose()) {
            window.update();
        }

        close();
    }

    private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {
        if (vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
        }
    }

    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo, VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger) {
        if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
            return vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
        }
        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    public void close() {
        if (ENABLE_DEBUG) {
            destroyDebugUtilsMessengerEXT(vInstance, debugMessenger, null);
        }
        vkDestroyCommandPool(logicalDevice.getLogicalDevice(), commandPool, null);
        vkDestroyDevice(logicalDevice.getLogicalDevice(), null);
        vkDestroySurfaceKHR(vInstance, surface, null);
        vkDestroyInstance(vInstance, null);
        window.cleanup();
    }

    private long setupDebugMessenger() {
        if (!ENABLE_DEBUG) {
            return -1;
        }

        try (MemoryStack stack = stackPush()) {
            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack);
            populateDebugMessengerCreateInfo(createInfo);
            LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);
            int res = createDebugUtilsMessengerEXT(vInstance, createInfo, null, pDebugMessenger);
            VkError.validate(res, "Failed to set up debug messenger");
            return pDebugMessenger.get(0);
        }
    }

    private long createSurface() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer surfaceBuffer = stack.longs(VK_NULL_HANDLE);
            int res = glfwCreateWindowSurface(vInstance, window.getInternalPointer(), null, surfaceBuffer);
            VkError.validate(res, "Failed to create window surface");

            return surfaceBuffer.get(0);
        }
    }

    @Override
    public void run() {

    }

    public static class Args {
        public final int width, height;

        public Args(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}