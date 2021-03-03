package org.sandboxpowered.silica.client.vulkan

import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackGet
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import org.sandboxpowered.api.util.math.Maths.clamp
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.Silica
import org.sandboxpowered.silica.client.util.ints
import org.sandboxpowered.silica.client.vulkan.VkError.Companion.checkError
import org.sandboxpowered.silica.util.set
import java.nio.IntBuffer
import java.util.stream.Collectors.toSet
import java.util.stream.IntStream


class VulkanRenderer(val silica: Silica) : Renderer {

    private lateinit var instance: VkInstance
    private var debugMessenger: Long = -1
    private var surface: Long = -1

    private lateinit var physicalDevice: VkPhysicalDevice
    private lateinit var device: VkDevice
    private lateinit var graphicsQueue: VkQueue
    private lateinit var presentQueue: VkQueue

    private var swapChain: Long = -1
    private lateinit var swapChainImages: LongArray
    private var swapChainImageFormat: Int = -1
    private lateinit var swapChainExtent: VkExtent2D

    private val deviceExtensions: Set<String> = setOf(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
    private val enableValidationLayers: Boolean = DEBUG[false]
    private val validationLayers: Set<String> = setOf("VK_LAYER_KHRONOS_validation")

    override fun init() {
        if (enableValidationLayers && !checkValidationLayerSupport()) {
            throw VkError("Validation requested but not supported")
        }
        createInstance()
        setupDebugMessenger()
        createSurface()
        pickPhysicalDevice()
        createLogicalDevice()
        createSwapChain()
    }

    private fun createSwapChain() {
        stackPush().use {
            val swapChainSupport = querySwapChainSupport(physicalDevice, it)

            val surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats)
            val presentMode = chooseSwapPresentMode(swapChainSupport.presentModes)
            val extent = chooseSwapExtent(swapChainSupport.capabilities)

            val imageCount = it.ints(swapChainSupport.capabilities.minImageCount() + 1)

            if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount[0] > swapChainSupport.capabilities.maxImageCount()) {
                imageCount[0] = swapChainSupport.capabilities.maxImageCount()
            }

            val createInfo = VkSwapchainCreateInfoKHR.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
            createInfo.surface(surface)

            createInfo.minImageCount(imageCount[0])
            createInfo.imageFormat(surfaceFormat.format())
            createInfo.imageColorSpace(surfaceFormat.colorSpace())
            createInfo.imageExtent(extent)
            createInfo.imageArrayLayers(1)
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)

            val indices = findQueueFamilies(physicalDevice)

            if (indices.graphicsFamily != indices.presentFamily) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                createInfo.pQueueFamilyIndices(it.ints(indices.array()))
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform())
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
            createInfo.presentMode(presentMode)
            createInfo.clipped(true)
            createInfo.oldSwapchain(VK_NULL_HANDLE)

            val pSwapChain = it.longs(VK_NULL_HANDLE)

            checkError("Failed to create swap chain", vkCreateSwapchainKHR(device, createInfo, null, pSwapChain))

            swapChain = pSwapChain[0]

            val pSwapChainImages = it.mallocLong(imageCount[0])

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, pSwapChainImages)

            swapChainImages = LongArray(imageCount[0]) { pSwapChainImages[it] }

            swapChainImageFormat = surfaceFormat.format()
            swapChainExtent = VkExtent2D.create().set(extent)
        }
    }

    private fun chooseSwapExtent(capabilities: VkSurfaceCapabilitiesKHR): VkExtent2D {
        if(capabilities.currentExtent().width() != Int.MAX_VALUE)
            return capabilities.currentExtent()

        val window = silica.window

        val actual = VkExtent2D.mallocStack().set(window.width, window.height)

        val min = capabilities.minImageExtent()
        val max = capabilities.maxImageExtent()

        actual.width(clamp(actual.width(), min.width(), max.width()))
        actual.height(clamp(actual.height(), min.height(), max.height()))

        return actual
    }

    private fun chooseSwapPresentMode(presentModes: IntBuffer): Int {
        for(i in 0 until presentModes.capacity()) {
            if(presentModes[i] == VK_PRESENT_MODE_MAILBOX_KHR)
                return presentModes[i]
        }

        return VK_PRESENT_MODE_FIFO_KHR
    }

    private fun chooseSwapSurfaceFormat(formats: VkSurfaceFormatKHR.Buffer): VkSurfaceFormatKHR {
        return formats.asSequence()
            .filter { it.format() == VK_FORMAT_B8G8R8_UNORM }
            .filter { it.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR }
            .firstOrNull() ?: formats[0]
    }

    private fun querySwapChainSupport(device: VkPhysicalDevice, it: MemoryStack): SwapchainSupportDetails {
        val details = SwapchainSupportDetails()

        details.capabilities = VkSurfaceCapabilitiesKHR.mallocStack(it)
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities)

        val count = it.ints(0)

        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null)

        if(count[0] != 0) {
            details.formats = VkSurfaceFormatKHR.mallocStack(count[0], it)
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats)
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(device,surface, count, null)

        if(count[0] != 0) {
            details.presentModes = it.mallocInt(count[0])
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes)
        }

        return details
    }

    private fun createSurface() {
        stackPush().use {
            val pSurface = it.longs(VK_NULL_HANDLE)

            checkError(
                "Failed to create window surface",
                glfwCreateWindowSurface(instance, silica.window.internalPointer, null, pSurface)
            )

            surface = pSurface[0]
        }
    }

    private fun createLogicalDevice() {
        stackPush().use {
            val indices = findQueueFamilies(physicalDevice)

            val uniqueQueueFamilies = indices.unique()

            val queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilies.size, it)

            for (i in uniqueQueueFamilies.indices) {
                val queueCreateInfo = queueCreateInfos[i]
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i])
                queueCreateInfo.pQueuePriorities(it.floats(1f))
            }

            val features = VkPhysicalDeviceFeatures.callocStack(it)

            val createInfo = VkDeviceCreateInfo.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
            createInfo.pQueueCreateInfos(queueCreateInfos)

            createInfo.pEnabledFeatures(features)

            createInfo.ppEnabledExtensionNames(asPointerBuffer(deviceExtensions))

            if (enableValidationLayers) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(validationLayers))
            }

            val pDevice = it.pointers(VK_NULL_HANDLE)

            checkError("Failed to create logical device", vkCreateDevice(physicalDevice, createInfo, null, pDevice))

            device = VkDevice(pDevice[0], physicalDevice, createInfo)

            val pQueue = it.pointers(VK_NULL_HANDLE)

            vkGetDeviceQueue(device, indices.graphicsFamily!!, 0, pQueue)
            graphicsQueue = VkQueue(pQueue[0], device)

            vkGetDeviceQueue(device, indices.presentFamily!!, 0, pQueue)
            presentQueue = VkQueue(pQueue[0], device)
        }
    }

    private fun createInstance() {
        stackPush().use {
            val appInfo = VkApplicationInfo.callocStack(it)

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            appInfo.pApplicationName(it.UTF8Safe("Silica"))
            appInfo.applicationVersion(VK_MAKE_VERSION(0, 1, 0))
            appInfo.pEngineName(it.UTF8Safe("Sandstone"))
            appInfo.engineVersion(VK_MAKE_VERSION(0, 1, 0))
            appInfo.apiVersion(VK_API_VERSION_1_0)

            val createInfo = VkInstanceCreateInfo.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            createInfo.pApplicationInfo(appInfo)
            createInfo.ppEnabledExtensionNames(getRequiredExtensions(it))
            if (enableValidationLayers) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(validationLayers))

                val debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(it)
                populateDebugMessengerCreateInfo(debugCreateInfo)
                createInfo.pNext(debugCreateInfo.address())
            }

            val instancePointer = it.mallocPointer(1)

            checkError("Failed to create instance", vkCreateInstance(createInfo, null, instancePointer))

            instance = VkInstance(instancePointer[0], createInfo)
        }
    }

    private fun pickPhysicalDevice() {
        stackPush().use {
            val deviceCount = it.ints(0)

            checkError("Failed to enumerate physical devices", vkEnumeratePhysicalDevices(instance, deviceCount, null))

            checkError("Failed to find GPUs with Vulkan support.", deviceCount[0]) { it == 0 }

            val physicalDevices = it.mallocPointer(deviceCount[0])

            checkError(
                "Failed to enumerate physical devices",
                vkEnumeratePhysicalDevices(instance, deviceCount, physicalDevices)
            )

            var device: VkPhysicalDevice? = null

            for (i in 0 until physicalDevices.capacity()) {
                device = VkPhysicalDevice(physicalDevices[i], instance)
                if (isDeviceSuitable(device)) {
                    break
                }
            }

            checkError("Failed to find a suitable GPU.", device) { device == null }

            physicalDevice = device!!
        }
    }

    private class QueueFamilyIndices {
        // We use Integer to use null as the empty value
        var graphicsFamily: Int? = null
        var presentFamily: Int? = null
        val isComplete: Boolean
            get() = graphicsFamily != null && presentFamily != null

        fun unique(): IntArray {
            return IntStream.of(graphicsFamily!!, presentFamily!!).distinct().toArray()
        }

        fun array(): IntArray {
            return intArrayOf(graphicsFamily!!, presentFamily!!)
        }
    }

    private class SwapchainSupportDetails {
        lateinit var capabilities: VkSurfaceCapabilitiesKHR
        lateinit var formats: VkSurfaceFormatKHR.Buffer
        lateinit var presentModes: IntBuffer
    }


    private fun isDeviceSuitable(device: VkPhysicalDevice): Boolean {

        val extensionsSupported = checkDeviceExtensionSupport(device)
        var swapChainAdequate = false

        if (extensionsSupported) {
            stackPush().use {
                val swapchainSupportDetails = querySwapChainSupport(device, it)
                swapChainAdequate =
                    swapchainSupportDetails.formats.hasRemaining() && swapchainSupportDetails.presentModes.hasRemaining()
            }
        }

        return findQueueFamilies(device).isComplete && extensionsSupported && swapChainAdequate
    }

    private fun checkDeviceExtensionSupport(device: VkPhysicalDevice): Boolean {
        stackPush().use {
            val extensionCount = it.ints(0)

            vkEnumerateDeviceExtensionProperties(device, null as String?, extensionCount, null)

            val availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), it)

            return availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet()).containsAll(deviceExtensions)
        }
    }

    private fun findQueueFamilies(device: VkPhysicalDevice): QueueFamilyIndices {
        val indices = QueueFamilyIndices()

        stackPush().use {
            val queueFamilyCount = it.ints(0)

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null)

            val queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount[0], it)

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies)

            val presentSupport = it.ints(VK_FALSE)

            var i = 0
            while (i < queueFamilies.capacity() || !indices.isComplete) {
                if (queueFamilies[i].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
                    indices.graphicsFamily = i
                }
                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport)
                if (presentSupport.get(0) == VK_TRUE) {
                    indices.presentFamily = i
                }
                i++
            }
        }

        return indices
    }

    private fun debugCallback(messageSeverity: Int, messageType: Int, pCallbackData: Long, pUserData: Long): Int {
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
        System.err.println("Validation layer: " + callbackData.pMessageString())
        return VK_FALSE
    }

    private fun populateDebugMessengerCreateInfo(debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT) {
        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
        debugCreateInfo.pfnUserCallback(this::debugCallback)
    }

    private fun getRequiredExtensions(stack: MemoryStack): PointerBuffer? {
        val glfwExtensions = glfwGetRequiredInstanceExtensions()
        if (enableValidationLayers) {
            val extensions = stack.mallocPointer(glfwExtensions!!.capacity() + 1)
            extensions.put(glfwExtensions)
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))

            return extensions.rewind()
        }
        return glfwExtensions
    }

    private fun checkValidationLayerSupport(): Boolean {
        stackPush().use { stack ->
            val layerCount = stack.ints(0)
            vkEnumerateInstanceLayerProperties(layerCount, null)
            val availableLayers = VkLayerProperties.mallocStack(layerCount[0], stack)
            vkEnumerateInstanceLayerProperties(layerCount, availableLayers)
            val availableLayerNames = availableLayers.asSequence().map { it.layerNameString() }.toSet()
            return validationLayers.let { availableLayerNames.containsAll(it) } ?: true
        }
    }

    private fun setupDebugMessenger() {
        if (!enableValidationLayers) {
            return
        }
        stackPush().use { stack ->
            val createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
            populateDebugMessengerCreateInfo(createInfo)
            val pDebugMessenger = stack.longs(VK_NULL_HANDLE)
            checkError(
                "Failed to set up debug messenger",
                vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger)
            )
            debugMessenger = pDebugMessenger[0]
        }
    }

    private fun asPointerBuffer(set: Set<String>): PointerBuffer {
        val stack = stackGet()
        val buffer = stack.mallocPointer(set.size)
        set.asSequence()
            .map(stack::UTF8)
            .forEach(buffer::put)
        return buffer.rewind()
    }

    override fun cleanup() {
        vkDestroyDevice(device, null)
        if (enableValidationLayers) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null)
        }
        vkDestroySurfaceKHR(instance, surface, null);
        vkDestroyInstance(instance, null)
    }

    override fun initWindowHints() {
        if (!GLFWVulkan.glfwVulkanSupported())
            throw VkError("Cant use Vulkan")
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    }

    override fun getName(): String = "Vulkan"

    class VulkanRenderingFactory : RenderingFactory {
        override fun getPriority(): Int = 600
        override fun getId(): String = "vulkan"
        override fun createRenderer(silica: Silica): Renderer = VulkanRenderer(silica)
    }
}